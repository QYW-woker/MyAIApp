package com.myaiapp.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * 云同步管理器
 * 目前实现本地备份/恢复功能，后续可扩展为真正的云同步
 */
class CloudSyncManager(private val context: Context) {

    private val storageManager = FileStorageManager(context)
    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_SYNC_ENABLED = "sync_enabled"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_SYNC_ON_WIFI_ONLY = "sync_on_wifi_only"
        private const val BACKUP_DIR = "backups"
    }

    // 同步状态
    data class SyncStatus(
        val lastSyncTime: Long = 0,
        val syncEnabled: Boolean = false,
        val autoSync: Boolean = false,
        val syncOnWifiOnly: Boolean = true,
        val hasLocalChanges: Boolean = false,
        val backupCount: Int = 0
    )

    // 备份数据结构
    data class BackupData(
        val version: Int = 1,
        val timestamp: Long = System.currentTimeMillis(),
        val deviceId: String = "",
        val books: List<AccountBook> = emptyList(),
        val transactions: Map<String, List<Transaction>> = emptyMap(),
        val categories: List<Category> = emptyList(),
        val budgets: List<Budget> = emptyList(),
        val savingsPlans: List<SavingsPlan> = emptyList(),
        val accounts: List<Account> = emptyList(),
        val currencies: List<Currency> = emptyList(),
        val settings: Settings? = null
    )

    /**
     * 获取同步状态
     */
    fun getSyncStatus(): SyncStatus {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        val backupCount = if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" }?.size ?: 0
        } else 0

        return SyncStatus(
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0),
            syncEnabled = prefs.getBoolean(KEY_SYNC_ENABLED, false),
            autoSync = prefs.getBoolean(KEY_AUTO_SYNC, false),
            syncOnWifiOnly = prefs.getBoolean(KEY_SYNC_ON_WIFI_ONLY, true),
            backupCount = backupCount
        )
    }

    /**
     * 更新同步设置
     */
    fun updateSyncSettings(
        syncEnabled: Boolean? = null,
        autoSync: Boolean? = null,
        syncOnWifiOnly: Boolean? = null
    ) {
        prefs.edit().apply {
            syncEnabled?.let { putBoolean(KEY_SYNC_ENABLED, it) }
            autoSync?.let { putBoolean(KEY_AUTO_SYNC, it) }
            syncOnWifiOnly?.let { putBoolean(KEY_SYNC_ON_WIFI_ONLY, it) }
            apply()
        }
    }

    /**
     * 创建本地备份
     */
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 收集所有数据
            val books = storageManager.getAccountBooks()
            val transactionsMap = mutableMapOf<String, List<Transaction>>()
            books.forEach { book ->
                transactionsMap[book.id] = storageManager.getTransactions(book.id)
            }

            val backupData = BackupData(
                version = 1,
                timestamp = System.currentTimeMillis(),
                deviceId = getDeviceId(),
                books = books,
                transactions = transactionsMap,
                categories = storageManager.getCategories(),
                budgets = storageManager.getBudgets(),
                savingsPlans = storageManager.getSavingsPlans(),
                accounts = storageManager.getAccounts(),
                currencies = storageManager.getCurrencies(),
                settings = storageManager.getSettings()
            )

            // 创建备份目录
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // 生成备份文件名
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")

            // 写入备份文件
            val jsonData = gson.toJson(backupData)
            backupFile.writeText(jsonData)

            // 更新最后同步时间
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis()).apply()

            // 清理旧备份（保留最近5个）
            cleanOldBackups(5)

            Result.success(backupFile.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取备份列表
     */
    fun getBackupList(): List<BackupInfo> {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) return emptyList()

        return backupDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { file ->
                try {
                    val content = file.readText()
                    val backup = gson.fromJson(content, BackupData::class.java)
                    BackupInfo(
                        fileName = file.name,
                        timestamp = backup.timestamp,
                        size = file.length(),
                        bookCount = backup.books.size,
                        transactionCount = backup.transactions.values.sumOf { it.size }
                    )
                } catch (e: Exception) {
                    BackupInfo(
                        fileName = file.name,
                        timestamp = file.lastModified(),
                        size = file.length(),
                        bookCount = 0,
                        transactionCount = 0
                    )
                }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * 从备份恢复数据
     */
    suspend fun restoreFromBackup(fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(File(context.filesDir, BACKUP_DIR), fileName)
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("备份文件不存在"))
            }

            val jsonData = backupFile.readText()
            val backupData = gson.fromJson(jsonData, BackupData::class.java)

            // 恢复账本
            backupData.books.forEach { book ->
                storageManager.saveAccountBook(book)
            }

            // 恢复交易记录
            backupData.transactions.forEach { (bookId, transactions) ->
                transactions.forEach { transaction ->
                    storageManager.saveTransaction(bookId, transaction)
                }
            }

            // 恢复分类
            storageManager.saveCategories(backupData.categories)

            // 恢复预算
            backupData.budgets.forEach { budget ->
                storageManager.saveBudget(budget)
            }

            // 恢复存钱计划
            backupData.savingsPlans.forEach { plan ->
                storageManager.saveSavingsPlan(plan)
            }

            // 恢复账户
            backupData.accounts.forEach { account ->
                storageManager.saveAccount(account)
            }

            // 恢复货币
            if (backupData.currencies.isNotEmpty()) {
                storageManager.saveCurrencies(backupData.currencies)
            }

            // 恢复设置
            backupData.settings?.let { settings ->
                storageManager.saveSettings(settings)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除备份
     */
    fun deleteBackup(fileName: String): Boolean {
        val backupFile = File(File(context.filesDir, BACKUP_DIR), fileName)
        return if (backupFile.exists()) {
            backupFile.delete()
        } else false
    }

    /**
     * 导出备份到外部存储
     */
    suspend fun exportBackup(fileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(File(context.filesDir, BACKUP_DIR), fileName)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(Exception("备份文件不存在"))
            }

            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val exportFile = File(exportDir, fileName)
            sourceFile.copyTo(exportFile, overwrite = true)

            Result.success(exportFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从外部文件导入备份
     */
    suspend fun importBackup(content: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 验证JSON格式
            val backupData = gson.fromJson(content, BackupData::class.java)

            // 创建备份目录
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // 生成备份文件名
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(java.util.Date(backupData.timestamp))
            val backupFile = File(backupDir, "imported_$timestamp.json")

            backupFile.writeText(content)

            Result.success(backupFile.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取数据摘要用于同步比对
     */
    suspend fun getDataChecksum(): String = withContext(Dispatchers.IO) {
        try {
            val books = storageManager.getAccountBooks()
            val transactionsCount = books.sumOf { storageManager.getTransactions(it.id).size }
            val categoriesCount = storageManager.getCategories().size
            val budgetsCount = storageManager.getBudgets().size
            val savingsCount = storageManager.getSavingsPlans().size

            val data = "$transactionsCount-$categoriesCount-$budgetsCount-$savingsCount"
            MessageDigest.getInstance("MD5")
                .digest(data.toByteArray())
                .joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 清理旧备份
     */
    private fun cleanOldBackups(keepCount: Int) {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) return

        val backups = backupDir.listFiles()
            ?.filter { it.extension == "json" && it.name.startsWith("backup_") }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (backups.size > keepCount) {
            backups.drop(keepCount).forEach { it.delete() }
        }
    }

    /**
     * 获取设备ID
     */
    private fun getDeviceId(): String {
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }
}

/**
 * 备份信息
 */
data class BackupInfo(
    val fileName: String,
    val timestamp: Long,
    val size: Long,
    val bookCount: Int,
    val transactionCount: Int
)
