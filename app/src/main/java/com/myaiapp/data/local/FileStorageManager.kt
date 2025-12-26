package com.myaiapp.data.local

import android.content.Context
import com.myaiapp.data.local.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * JSON文件存储管理器
 * 负责所有数据的读写操作
 */
class FileStorageManager(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val baseDir: File
        get() = File(context.filesDir, "MyAIAPP").apply {
            if (!exists()) mkdirs()
        }

    // ===== 目录结构 =====
    private val configDir get() = File(baseDir, "config").apply { if (!exists()) mkdirs() }
    private val accountsDir get() = File(baseDir, "accounts").apply { if (!exists()) mkdirs() }
    private val budgetDir get() = File(baseDir, "budget").apply { if (!exists()) mkdirs() }
    private val savingsDir get() = File(baseDir, "savings").apply { if (!exists()) mkdirs() }
    private val remindersDir get() = File(baseDir, "reminders").apply { if (!exists()) mkdirs() }
    private val backupDir get() = File(baseDir, "backup").apply { if (!exists()) mkdirs() }

    private fun getRecordsDir(bookId: String): File {
        return File(baseDir, "records/$bookId").apply { if (!exists()) mkdirs() }
    }

    // ===== 设置 =====
    private val settingsFile get() = File(configDir, "settings.json")
    private val aiConfigFile get() = File(configDir, "ai_config.json")
    private val categoriesFile get() = File(configDir, "categories.json")
    private val currenciesFile get() = File(configDir, "currencies.json")

    // ===== 账户 =====
    private val accountBooksFile get() = File(accountsDir, "account_books.json")
    private val assetAccountsFile get() = File(accountsDir, "asset_accounts.json")
    private val currentBookFile get() = File(accountsDir, "current_book.json")

    // ===== 预算 =====
    private val budgetsFile get() = File(budgetDir, "budgets.json")

    // ===== 存钱计划 =====
    private val savingsPlansFile get() = File(savingsDir, "savings_plans.json")

    // ===== 提醒 =====
    private val remindersFile get() = File(remindersDir, "reminders.json")

    // ===== 记录文件 =====
    private fun getTransactionsFile(bookId: String) = File(getRecordsDir(bookId), "transactions.json")
    private fun getTemplatesFile(bookId: String) = File(getRecordsDir(bookId), "templates.json")
    private fun getRecurringFile(bookId: String) = File(getRecordsDir(bookId), "recurring.json")

    // ==================== 通用读写方法 ====================

    private suspend inline fun <reified T> readFile(file: File, default: T): T = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                val content = file.readText()
                if (content.isNotBlank()) {
                    json.decodeFromString<T>(content)
                } else {
                    default
                }
            } else {
                default
            }
        } catch (e: Exception) {
            e.printStackTrace()
            default
        }
    }

    private suspend inline fun <reified T> writeFile(file: File, data: T) = withContext(Dispatchers.IO) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== 设置 ====================

    suspend fun getSettings(): AppSettings = readFile(settingsFile, AppSettings())

    suspend fun saveSettings(settings: AppSettings) = writeFile(settingsFile, settings)

    // ==================== AI配置 ====================

    suspend fun getAIConfig(): AIConfig = readFile(aiConfigFile, AIConfig())

    suspend fun saveAIConfig(config: AIConfig) = writeFile(aiConfigFile, config)

    // ==================== 分类 ====================

    suspend fun getCategories(): List<Category> {
        val data = readFile(categoriesFile, CategoriesData())
        return if (data.categories.isEmpty()) {
            val defaults = getDefaultCategories()
            saveCategories(defaults)
            defaults
        } else {
            data.categories
        }
    }

    suspend fun saveCategories(categories: List<Category>) {
        writeFile(categoriesFile, CategoriesData(categories))
    }

    suspend fun addCategory(category: Category) {
        val current = getCategories().toMutableList()
        current.add(category)
        saveCategories(current)
    }

    suspend fun updateCategory(category: Category) {
        val current = getCategories().toMutableList()
        val index = current.indexOfFirst { it.id == category.id }
        if (index >= 0) {
            current[index] = category
            saveCategories(current)
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        val current = getCategories().filter { it.id != categoryId }
        saveCategories(current)
    }

    // ==================== 账本 ====================

    suspend fun getAccountBooks(): List<AccountBook> {
        val data = readFile(accountBooksFile, AccountBooksData())
        return if (data.books.isEmpty()) {
            val defaultBook = AccountBook(
                name = "默认账本",
                icon = "book",
                color = "#5B8DEF",
                isDefault = true
            )
            saveAccountBooks(listOf(defaultBook))
            setCurrentBookId(defaultBook.id)
            listOf(defaultBook)
        } else {
            data.books
        }
    }

    suspend fun saveAccountBooks(books: List<AccountBook>) {
        writeFile(accountBooksFile, AccountBooksData(books))
    }

    suspend fun addAccountBook(book: AccountBook) {
        val current = getAccountBooks().toMutableList()
        current.add(book)
        saveAccountBooks(current)
    }

    suspend fun updateAccountBook(book: AccountBook) {
        val current = getAccountBooks().toMutableList()
        val index = current.indexOfFirst { it.id == book.id }
        if (index >= 0) {
            current[index] = book
            saveAccountBooks(current)
        }
    }

    suspend fun deleteAccountBook(bookId: String) {
        val current = getAccountBooks().filter { it.id != bookId }
        saveAccountBooks(current)
        // 删除相关记录目录
        File(baseDir, "records/$bookId").deleteRecursively()
    }

    // ==================== 当前账本 ====================

    suspend fun getCurrentBookId(): String {
        val data = readFile(currentBookFile, CurrentBookData(""))
        return if (data.bookId.isBlank()) {
            val books = getAccountBooks()
            val defaultBook = books.find { it.isDefault } ?: books.firstOrNull()
            defaultBook?.id ?: ""
        } else {
            data.bookId
        }
    }

    suspend fun setCurrentBookId(bookId: String) {
        writeFile(currentBookFile, CurrentBookData(bookId))
    }

    // ==================== 资产账户 ====================

    suspend fun getAssetAccounts(): List<AssetAccount> {
        val data = readFile(assetAccountsFile, AssetAccountsData())
        return if (data.accounts.isEmpty()) {
            val defaults = getDefaultAssetAccounts()
            saveAssetAccounts(defaults)
            defaults
        } else {
            data.accounts
        }
    }

    suspend fun saveAssetAccounts(accounts: List<AssetAccount>) {
        writeFile(assetAccountsFile, AssetAccountsData(accounts))
    }

    suspend fun addAssetAccount(account: AssetAccount) {
        val current = getAssetAccounts().toMutableList()
        current.add(account)
        saveAssetAccounts(current)
    }

    suspend fun updateAssetAccount(account: AssetAccount) {
        val current = getAssetAccounts().toMutableList()
        val index = current.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            current[index] = account
            saveAssetAccounts(current)
        }
    }

    suspend fun deleteAssetAccount(accountId: String) {
        val current = getAssetAccounts().filter { it.id != accountId }
        saveAssetAccounts(current)
    }

    suspend fun updateAccountBalance(accountId: String, newBalance: Double) {
        val current = getAssetAccounts().toMutableList()
        val index = current.indexOfFirst { it.id == accountId }
        if (index >= 0) {
            current[index] = current[index].copy(balance = newBalance)
            saveAssetAccounts(current)
        }
    }

    // ==================== 交易记录 ====================

    suspend fun getTransactions(bookId: String): List<Transaction> {
        return readFile(getTransactionsFile(bookId), TransactionsData()).transactions
    }

    suspend fun saveTransactions(bookId: String, transactions: List<Transaction>) {
        writeFile(getTransactionsFile(bookId), TransactionsData(transactions))
    }

    suspend fun addTransaction(transaction: Transaction) {
        val current = getTransactions(transaction.bookId).toMutableList()
        current.add(transaction)
        saveTransactions(transaction.bookId, current)

        // 更新账户余额
        updateBalanceForTransaction(transaction, true)
    }

    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        // 先回滚旧交易的余额影响
        updateBalanceForTransaction(oldTransaction, false)

        // 更新交易记录
        val current = getTransactions(newTransaction.bookId).toMutableList()
        val index = current.indexOfFirst { it.id == newTransaction.id }
        if (index >= 0) {
            current[index] = newTransaction
            saveTransactions(newTransaction.bookId, current)
        }

        // 应用新交易的余额影响
        updateBalanceForTransaction(newTransaction, true)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val current = getTransactions(transaction.bookId).filter { it.id != transaction.id }
        saveTransactions(transaction.bookId, current)

        // 回滚余额
        updateBalanceForTransaction(transaction, false)
    }

    private suspend fun updateBalanceForTransaction(transaction: Transaction, isAdd: Boolean) {
        val multiplier = if (isAdd) 1 else -1
        val accounts = getAssetAccounts().toMutableList()

        when (transaction.type) {
            TransactionType.INCOME -> {
                val index = accounts.indexOfFirst { it.id == transaction.accountId }
                if (index >= 0) {
                    accounts[index] = accounts[index].copy(
                        balance = accounts[index].balance + transaction.amount * multiplier
                    )
                }
            }
            TransactionType.EXPENSE -> {
                val index = accounts.indexOfFirst { it.id == transaction.accountId }
                if (index >= 0) {
                    accounts[index] = accounts[index].copy(
                        balance = accounts[index].balance - transaction.amount * multiplier
                    )
                }
            }
            TransactionType.TRANSFER -> {
                // 从源账户扣除
                val fromIndex = accounts.indexOfFirst { it.id == transaction.accountId }
                if (fromIndex >= 0) {
                    accounts[fromIndex] = accounts[fromIndex].copy(
                        balance = accounts[fromIndex].balance - transaction.amount * multiplier
                    )
                }
                // 向目标账户添加
                transaction.toAccountId?.let { toId ->
                    val toIndex = accounts.indexOfFirst { it.id == toId }
                    if (toIndex >= 0) {
                        accounts[toIndex] = accounts[toIndex].copy(
                            balance = accounts[toIndex].balance + transaction.amount * multiplier
                        )
                    }
                }
            }
        }

        saveAssetAccounts(accounts)
    }

    // ==================== 模板 ====================

    suspend fun getTemplates(bookId: String): List<RecordTemplate> {
        return readFile(getTemplatesFile(bookId), TemplatesData()).templates
    }

    suspend fun saveTemplates(bookId: String, templates: List<RecordTemplate>) {
        writeFile(getTemplatesFile(bookId), TemplatesData(templates))
    }

    suspend fun addTemplate(bookId: String, template: RecordTemplate) {
        val current = getTemplates(bookId).toMutableList()
        current.add(template)
        saveTemplates(bookId, current)
    }

    suspend fun deleteTemplate(bookId: String, templateId: String) {
        val current = getTemplates(bookId).filter { it.id != templateId }
        saveTemplates(bookId, current)
    }

    // ==================== 自动重复 ====================

    suspend fun getRecurringTransactions(bookId: String): List<RecurringTransaction> {
        return readFile(getRecurringFile(bookId), RecurringTransactionsData()).recurring
    }

    suspend fun saveRecurringTransactions(bookId: String, recurring: List<RecurringTransaction>) {
        writeFile(getRecurringFile(bookId), RecurringTransactionsData(recurring))
    }

    // ==================== 预算 ====================

    suspend fun getBudgets(): List<Budget> {
        return readFile(budgetsFile, BudgetsData()).budgets
    }

    suspend fun saveBudgets(budgets: List<Budget>) {
        writeFile(budgetsFile, BudgetsData(budgets))
    }

    suspend fun addBudget(budget: Budget) {
        val current = getBudgets().toMutableList()
        current.add(budget)
        saveBudgets(current)
    }

    suspend fun updateBudget(budget: Budget) {
        val current = getBudgets().toMutableList()
        val index = current.indexOfFirst { it.id == budget.id }
        if (index >= 0) {
            current[index] = budget
            saveBudgets(current)
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        val current = getBudgets().filter { it.id != budgetId }
        saveBudgets(current)
    }

    // ==================== 存钱计划 ====================

    suspend fun getSavingsPlans(): List<SavingsPlan> {
        return readFile(savingsPlansFile, SavingsPlansData()).plans
    }

    suspend fun saveSavingsPlans(plans: List<SavingsPlan>) {
        writeFile(savingsPlansFile, SavingsPlansData(plans))
    }

    suspend fun addSavingsPlan(plan: SavingsPlan) {
        val current = getSavingsPlans().toMutableList()
        current.add(plan)
        saveSavingsPlans(current)
    }

    suspend fun updateSavingsPlan(plan: SavingsPlan) {
        val current = getSavingsPlans().toMutableList()
        val index = current.indexOfFirst { it.id == plan.id }
        if (index >= 0) {
            current[index] = plan
            saveSavingsPlans(current)
        }
    }

    suspend fun deleteSavingsPlan(planId: String) {
        val current = getSavingsPlans().filter { it.id != planId }
        saveSavingsPlans(current)
    }

    suspend fun addDepositToPlan(planId: String, deposit: SavingsDeposit) {
        val current = getSavingsPlans().toMutableList()
        val index = current.indexOfFirst { it.id == planId }
        if (index >= 0) {
            val plan = current[index]
            val updatedDeposits = plan.deposits + deposit
            current[index] = plan.copy(
                deposits = updatedDeposits,
                currentAmount = plan.currentAmount + deposit.amount
            )
            saveSavingsPlans(current)
        }
    }

    // ==================== 提醒 ====================

    suspend fun getReminders(): List<Reminder> {
        return readFile(remindersFile, RemindersData()).reminders
    }

    suspend fun saveReminders(reminders: List<Reminder>) {
        writeFile(remindersFile, RemindersData(reminders))
    }

    suspend fun addReminder(reminder: Reminder) {
        val current = getReminders().toMutableList()
        current.add(reminder)
        saveReminders(current)
    }

    suspend fun deleteReminder(reminderId: String) {
        val current = getReminders().filter { it.id != reminderId }
        saveReminders(current)
    }

    // ==================== 货币 ====================

    suspend fun getCurrencies(): List<Currency> {
        val data = readFile(currenciesFile, CurrenciesData())
        return if (data.currencies.isEmpty()) {
            val defaults = getDefaultCurrencies()
            saveCurrencies(defaults)
            defaults
        } else {
            data.currencies
        }
    }

    suspend fun saveCurrencies(currencies: List<Currency>) {
        writeFile(currenciesFile, CurrenciesData(currencies))
    }

    // ==================== 备份与恢复 ====================

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val backupFile = File(backupDir, "backup_$timestamp.zip")
        // 实现ZIP备份逻辑
        backupFile
    }

    suspend fun restoreFromBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        // 实现恢复逻辑
        true
    }

    // ==================== 默认数据 ====================

    private fun getDefaultCategories(): List<Category> = listOf(
        // 支出分类
        Category("exp_food", "餐饮", TransactionType.EXPENSE, "restaurant", "#FFAA5B", isSystem = true, sortOrder = 1),
        Category("exp_shopping", "购物", TransactionType.EXPENSE, "shopping_bag", "#FF6B6B", isSystem = true, sortOrder = 2),
        Category("exp_transport", "交通", TransactionType.EXPENSE, "train", "#5B8DEF", isSystem = true, sortOrder = 3),
        Category("exp_entertainment", "娱乐", TransactionType.EXPENSE, "gamepad", "#A78BFA", isSystem = true, sortOrder = 4),
        Category("exp_housing", "居住", TransactionType.EXPENSE, "home", "#2DD4BF", isSystem = true, sortOrder = 5),
        Category("exp_medical", "医疗", TransactionType.EXPENSE, "activity", "#FF6B6B", isSystem = true, sortOrder = 6),
        Category("exp_education", "教育", TransactionType.EXPENSE, "graduation_cap", "#5B8DEF", isSystem = true, sortOrder = 7),
        Category("exp_communication", "通讯", TransactionType.EXPENSE, "phone", "#737373", isSystem = true, sortOrder = 8),
        Category("exp_beauty", "美容", TransactionType.EXPENSE, "sparkles", "#F472B6", isSystem = true, sortOrder = 9),
        Category("exp_sports", "运动", TransactionType.EXPENSE, "dumbbell", "#4CD964", isSystem = true, sortOrder = 10),
        Category("exp_social", "社交", TransactionType.EXPENSE, "users", "#FFAA5B", isSystem = true, sortOrder = 11),
        Category("exp_travel", "旅行", TransactionType.EXPENSE, "plane", "#5B8DEF", isSystem = true, sortOrder = 12),
        Category("exp_pet", "宠物", TransactionType.EXPENSE, "paw_print", "#FFAA5B", isSystem = true, sortOrder = 13),
        Category("exp_gift", "礼物", TransactionType.EXPENSE, "gift", "#F472B6", isSystem = true, sortOrder = 14),
        Category("exp_other", "其他", TransactionType.EXPENSE, "more_horizontal", "#A3A3A3", isSystem = true, sortOrder = 15),

        // 收入分类
        Category("inc_salary", "工资", TransactionType.INCOME, "briefcase", "#4CD964", isSystem = true, sortOrder = 1),
        Category("inc_bonus", "奖金", TransactionType.INCOME, "award", "#4CD964", isSystem = true, sortOrder = 2),
        Category("inc_sideline", "副业", TransactionType.INCOME, "laptop", "#2DD4BF", isSystem = true, sortOrder = 3),
        Category("inc_investment", "投资", TransactionType.INCOME, "trending_up", "#A78BFA", isSystem = true, sortOrder = 4),
        Category("inc_interest", "利息", TransactionType.INCOME, "landmark", "#5B8DEF", isSystem = true, sortOrder = 5),
        Category("inc_gift", "礼金", TransactionType.INCOME, "heart", "#FFAA5B", isSystem = true, sortOrder = 6),
        Category("inc_refund", "退款", TransactionType.INCOME, "rotate_ccw", "#5B8DEF", isSystem = true, sortOrder = 7),
        Category("inc_other", "其他", TransactionType.INCOME, "more_horizontal", "#A3A3A3", isSystem = true, sortOrder = 8)
    )

    private fun getDefaultAssetAccounts(): List<AssetAccount> = listOf(
        AssetAccount(
            id = "default_cash",
            name = "现金",
            type = AssetType.CASH,
            balance = 0.0,
            icon = "wallet",
            color = "#4CD964"
        ),
        AssetAccount(
            id = "default_alipay",
            name = "支付宝",
            type = AssetType.ALIPAY,
            balance = 0.0,
            icon = "smartphone",
            color = "#5B8DEF"
        ),
        AssetAccount(
            id = "default_wechat",
            name = "微信",
            type = AssetType.WECHAT,
            balance = 0.0,
            icon = "message_circle",
            color = "#4CD964"
        )
    )

    private fun getDefaultCurrencies(): List<Currency> = listOf(
        Currency("CNY", "人民币", "¥", 1.0),
        Currency("USD", "美元", "$", 7.2),
        Currency("EUR", "欧元", "€", 7.8),
        Currency("GBP", "英镑", "£", 9.1),
        Currency("JPY", "日元", "¥", 0.048),
        Currency("HKD", "港币", "HK$", 0.92),
        Currency("TWD", "新台币", "NT$", 0.22),
        Currency("KRW", "韩元", "₩", 0.0054)
    )
}
