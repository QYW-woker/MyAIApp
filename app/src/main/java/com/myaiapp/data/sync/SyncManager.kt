package com.myaiapp.data.sync

import android.content.Context
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.SyncConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 同步状态
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * 同步管理器
 * 支持 WebDAV 云同步
 */
class SyncManager(
    private val context: Context,
    private val storageManager: FileStorageManager
) {
    private val dataDir: File
        get() = File(context.filesDir, "MyAIAPP")

    private val remoteSyncPath = "/MyAIApp"
    private val syncFileName = "sync_data.zip"
    private val metaFileName = "sync_meta.json"

    /**
     * 获取 WebDAV 客户端
     */
    private suspend fun getClient(): WebDavClient? {
        val config = storageManager.getSyncConfig()
        if (!config.enabled || config.serverUrl.isBlank() || config.username.isBlank()) {
            return null
        }
        return WebDavClient(config.serverUrl, config.username, config.password)
    }

    /**
     * 测试连接
     */
    suspend fun testConnection(config: SyncConfig): Result<Boolean> {
        val client = WebDavClient(config.serverUrl, config.username, config.password)
        return client.testConnection()
    }

    /**
     * 上传数据到云端
     */
    suspend fun uploadToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = getClient() ?: return@withContext Result.failure(Exception("未配置同步"))

            // 创建同步目录
            client.createDirectory(remoteSyncPath)

            // 创建数据压缩包
            val zipData = createSyncPackage()

            // 上传数据
            val uploadResult = client.upload(
                "$remoteSyncPath/$syncFileName",
                zipData,
                "application/zip"
            )

            if (uploadResult.isFailure) {
                return@withContext Result.failure(uploadResult.exceptionOrNull()!!)
            }

            // 上传同步元信息
            val meta = SyncMeta(
                lastSyncTime = System.currentTimeMillis(),
                deviceId = getDeviceId(),
                version = 1
            )
            val metaJson = com.google.gson.Gson().toJson(meta)
            client.upload(
                "$remoteSyncPath/$metaFileName",
                metaJson.toByteArray(),
                "application/json"
            )

            // 更新本地同步时间
            val config = storageManager.getSyncConfig()
            storageManager.saveSyncConfig(config.copy(lastSyncTime = System.currentTimeMillis()))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从云端下载数据
     */
    suspend fun downloadFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = getClient() ?: return@withContext Result.failure(Exception("未配置同步"))

            // 检查远程文件是否存在
            if (!client.exists("$remoteSyncPath/$syncFileName")) {
                return@withContext Result.failure(Exception("云端没有同步数据"))
            }

            // 下载数据
            val downloadResult = client.download("$remoteSyncPath/$syncFileName")
            if (downloadResult.isFailure) {
                return@withContext Result.failure(downloadResult.exceptionOrNull()!!)
            }

            val zipData = downloadResult.getOrNull()!!

            // 先备份当前数据
            val backupDir = File(context.cacheDir, "sync_backup")
            if (backupDir.exists()) backupDir.deleteRecursively()
            if (dataDir.exists()) {
                dataDir.copyRecursively(backupDir, overwrite = true)
            }

            try {
                // 解压并恢复数据
                extractSyncPackage(zipData)

                // 更新本地同步时间
                val config = storageManager.getSyncConfig()
                storageManager.saveSyncConfig(config.copy(lastSyncTime = System.currentTimeMillis()))

                // 清理备份
                backupDir.deleteRecursively()

                Result.success(Unit)
            } catch (e: Exception) {
                // 恢复失败，还原备份
                if (backupDir.exists()) {
                    if (dataDir.exists()) dataDir.deleteRecursively()
                    backupDir.copyRecursively(dataDir, overwrite = true)
                    backupDir.deleteRecursively()
                }
                throw e
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取云端同步信息
     */
    suspend fun getCloudSyncInfo(): SyncMeta? = withContext(Dispatchers.IO) {
        try {
            val client = getClient() ?: return@withContext null

            val downloadResult = client.download("$remoteSyncPath/$metaFileName")
            if (downloadResult.isFailure) return@withContext null

            val metaJson = String(downloadResult.getOrNull()!!)
            com.google.gson.Gson().fromJson(metaJson, SyncMeta::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 创建同步数据包
     */
    private fun createSyncPackage(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
            val dirsToSync = listOf("config", "accounts", "records", "budget", "savings", "reminders")

            for (dirName in dirsToSync) {
                val dir = File(dataDir, dirName)
                if (dir.exists() && dir.isDirectory) {
                    addDirectoryToZip(zos, dir, dirName)
                }
            }
        }
        return outputStream.toByteArray()
    }

    /**
     * 解压同步数据包
     */
    private fun extractSyncPackage(zipData: ByteArray) {
        val tempDir = File(context.cacheDir, "sync_temp")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        // 解压到临时目录
        ZipInputStream(BufferedInputStream(ByteArrayInputStream(zipData))).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(tempDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                entry = zis.nextEntry
            }
        }

        // 覆盖数据目录
        val dirsToRestore = listOf("config", "accounts", "records", "budget", "savings", "reminders")
        for (dirName in dirsToRestore) {
            val sourceDir = File(tempDir, dirName)
            val targetDir = File(dataDir, dirName)

            if (sourceDir.exists()) {
                if (targetDir.exists()) targetDir.deleteRecursively()
                sourceDir.copyRecursively(targetDir, overwrite = true)
            }
        }

        // 清理临时目录
        tempDir.deleteRecursively()
    }

    private fun addDirectoryToZip(zos: ZipOutputStream, dir: File, basePath: String) {
        dir.listFiles()?.forEach { file ->
            val entryPath = "$basePath/${file.name}"
            if (file.isDirectory) {
                addDirectoryToZip(zos, file, entryPath)
            } else {
                zos.putNextEntry(ZipEntry(entryPath))
                FileInputStream(file).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
}

/**
 * 同步元信息
 */
data class SyncMeta(
    val lastSyncTime: Long,
    val deviceId: String,
    val version: Int
) {
    val formattedTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(lastSyncTime))
}
