package com.myaiapp.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 数据备份管理器
 */
class BackupManager(private val context: Context) {

    private val dataDir: File
        get() = File(context.filesDir, "MyAIAPP")

    private val backupDir: File
        get() = File(dataDir, "backup").apply { if (!exists()) mkdirs() }

    sealed class BackupResult {
        data class Success(val file: File) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }

    /**
     * 创建备份
     */
    suspend fun createBackup(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.zip")

            ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zos ->
                // 备份所有数据目录
                val dirsToBackup = listOf("config", "accounts", "records", "budget", "savings", "reminders")

                for (dirName in dirsToBackup) {
                    val dir = File(dataDir, dirName)
                    if (dir.exists() && dir.isDirectory) {
                        addDirectoryToZip(zos, dir, dirName)
                    }
                }
            }

            BackupResult.Success(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e.message ?: "备份失败")
        }
    }

    /**
     * 导出备份到指定位置
     */
    suspend fun exportBackup(destinationUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupResult = createBackup()
            if (backupResult is BackupResult.Error) {
                return@withContext backupResult
            }

            val backupFile = (backupResult as BackupResult.Success).file

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                FileInputStream(backupFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // 删除临时备份文件
            backupFile.delete()

            BackupResult.Success(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 从备份恢复
     */
    suspend fun restoreFromBackup(backupUri: Uri): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(backupUri)
                ?: return@withContext RestoreResult.Error("无法打开备份文件")

            // 先创建临时目录解压
            val tempDir = File(context.cacheDir, "restore_temp")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            // 解压备份
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
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

            // 验证备份数据
            val requiredDirs = listOf("config", "accounts")
            val hasValidData = requiredDirs.any { File(tempDir, it).exists() }
            if (!hasValidData) {
                tempDir.deleteRecursively()
                return@withContext RestoreResult.Error("无效的备份文件")
            }

            // 备份当前数据（以防恢复失败）
            val currentBackup = File(context.cacheDir, "current_backup")
            if (currentBackup.exists()) currentBackup.deleteRecursively()
            if (dataDir.exists()) {
                dataDir.copyRecursively(currentBackup, overwrite = true)
            }

            // 清空当前数据并恢复
            val dirsToRestore = listOf("config", "accounts", "records", "budget", "savings", "reminders")
            for (dirName in dirsToRestore) {
                val targetDir = File(dataDir, dirName)
                val sourceDir = File(tempDir, dirName)

                if (sourceDir.exists()) {
                    if (targetDir.exists()) targetDir.deleteRecursively()
                    sourceDir.copyRecursively(targetDir, overwrite = true)
                }
            }

            // 清理临时文件
            tempDir.deleteRecursively()
            currentBackup.deleteRecursively()

            RestoreResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            RestoreResult.Error(e.message ?: "恢复失败")
        }
    }

    /**
     * 获取本地备份列表
     */
    fun getLocalBackups(): List<BackupInfo> {
        return backupDir.listFiles()
            ?.filter { it.extension == "zip" && it.name.startsWith("backup_") }
            ?.map { file ->
                val timestamp = file.name
                    .removePrefix("backup_")
                    .removeSuffix(".zip")
                val date = try {
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .parse(timestamp)
                } catch (e: Exception) {
                    Date(file.lastModified())
                }

                BackupInfo(
                    file = file,
                    date = date ?: Date(),
                    size = file.length()
                )
            }
            ?.sortedByDescending { it.date }
            ?: emptyList()
    }

    /**
     * 删除本地备份
     */
    fun deleteBackup(file: File): Boolean {
        return file.delete()
    }

    /**
     * 清理旧备份，只保留最近N个
     */
    fun cleanOldBackups(keepCount: Int = 5) {
        val backups = getLocalBackups()
        if (backups.size > keepCount) {
            backups.drop(keepCount).forEach { it.file.delete() }
        }
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
}

data class BackupInfo(
    val file: File,
    val date: Date,
    val size: Long
) {
    val formattedSize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }

    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
}
