package com.myaiapp.data.local.model

import kotlinx.serialization.Serializable
import java.util.UUID

// ===== 交易类型 =====
@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

// ===== 资产类型 =====
@Serializable
enum class AssetType {
    CASH,           // 现金
    DEBIT_CARD,     // 储蓄卡
    CREDIT_CARD,    // 信用卡
    ALIPAY,         // 支付宝
    WECHAT,         // 微信
    INVESTMENT,     // 投资账户
    RECEIVABLE,     // 应收款
    PAYABLE         // 应付款
}

// ===== 预算周期 =====
@Serializable
enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY
}

// ===== 预算类型 =====
@Serializable
enum class BudgetType {
    TOTAL,
    CATEGORY
}

// ===== 存钱类型 =====
@Serializable
enum class SavingsType {
    FIXED,      // 定额存钱
    FLEXIBLE    // 灵活存钱
}

// ===== 重复频率 =====
@Serializable
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

// ===== 提醒类型 =====
@Serializable
enum class ReminderType {
    RECORD,         // 记账提醒
    CREDIT_CARD,    // 信用卡还款
    DEBT,           // 债务还款
    BUDGET,         // 预算提醒
    SAVINGS,        // 存钱提醒
    CUSTOM          // 自定义
}

// ===== 账本 =====
@Serializable
data class AccountBook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "book",
    val color: String = "#5B8DEF",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

// ===== 资产账户 =====
@Serializable
data class AssetAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AssetType,
    val balance: Double = 0.0,
    val icon: String,
    val color: String,
    val currency: String = "CNY",
    val creditLimit: Double? = null,      // 信用卡额度
    val billDay: Int? = null,             // 账单日
    val repaymentDay: Int? = null,        // 还款日
    val interestRate: Double? = null,     // 利率
    val includeInTotal: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ===== 交易记录 =====
@Serializable
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val categoryId: String,
    val accountId: String,
    val toAccountId: String? = null,      // 转账目标账户
    val bookId: String,
    val date: Long,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val currency: String = "CNY",
    val exchangeRate: Double = 1.0,
    val isRefund: Boolean = false,
    val refundFromId: String? = null,     // 退款来源
    val createdAt: Long = System.currentTimeMillis()
)

// ===== 分类 =====
@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: String,
    val color: String,
    val parentId: String? = null,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0
)

// ===== 预算 =====
@Serializable
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: BudgetType,
    val categoryId: String? = null,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val alertThreshold: Double = 0.8,
    val rollover: Boolean = false,
    val bookId: String
)

// ===== 存钱计划 =====
@Serializable
data class SavingsPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "💰",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val type: SavingsType,
    val fixedAmount: Double? = null,
    val frequency: String? = null,
    val targetDate: Long? = null,
    val deposits: List<SavingsDeposit> = emptyList(),
    val color: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis()
)

// ===== 存钱记录 =====
@Serializable
data class SavingsDeposit(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val date: Long,
    val note: String = ""
)

// ===== 记账模板 =====
@Serializable
data class RecordTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TransactionType,
    val amount: Double?,
    val categoryId: String,
    val accountId: String,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val useCount: Int = 0
)

// ===== 自动重复记账 =====
@Serializable
data class RecurringTransaction(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val frequency: RecurringFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val lastExecuted: Long? = null,
    val isActive: Boolean = true
)

// ===== 提醒 =====
@Serializable
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val type: ReminderType,
    val title: String,
    val content: String,
    val time: Long,
    val repeatType: String = "NONE",
    val isEnabled: Boolean = true,
    val relatedId: String? = null
)

// ===== 应用设置 =====
@Serializable
data class AppSettings(
    val defaultBookId: String = "",
    val defaultCurrency: String = "CNY",
    val startDayOfMonth: Int = 1,
    val startDayOfWeek: Int = 1,  // 1 = Monday
    val enableBiometric: Boolean = false,
    val enablePin: Boolean = false,
    val pinCode: String = "",
    val darkMode: String = "system",  // system, light, dark
    val language: String = "zh",
    val enableNotifications: Boolean = true,
    val enableBudgetAlert: Boolean = true,
    val budgetAlertThreshold: Double = 0.8
)

// ===== AI配置 =====
@Serializable
data class AIConfig(
    val provider: String = "deepseek",  // deepseek, groq
    val apiKey: String = "",
    val baseUrl: String = "https://api.deepseek.com",
    val model: String = "deepseek-chat",
    val enableAutoClassify: Boolean = true,
    val enableOCR: Boolean = true
)

// ===== 货币 =====
@Serializable
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val rate: Double = 1.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

// ===== 数据容器 =====
@Serializable
data class AccountBooksData(
    val books: List<AccountBook> = emptyList()
)

@Serializable
data class AssetAccountsData(
    val accounts: List<AssetAccount> = emptyList()
)

@Serializable
data class TransactionsData(
    val transactions: List<Transaction> = emptyList()
)

@Serializable
data class CategoriesData(
    val categories: List<Category> = emptyList()
)

@Serializable
data class BudgetsData(
    val budgets: List<Budget> = emptyList()
)

@Serializable
data class SavingsPlansData(
    val plans: List<SavingsPlan> = emptyList()
)

@Serializable
data class TemplatesData(
    val templates: List<RecordTemplate> = emptyList()
)

@Serializable
data class RecurringTransactionsData(
    val recurring: List<RecurringTransaction> = emptyList()
)

@Serializable
data class RemindersData(
    val reminders: List<Reminder> = emptyList()
)

@Serializable
data class CurrenciesData(
    val currencies: List<Currency> = emptyList()
)

@Serializable
data class CurrentBookData(
    val bookId: String
)
