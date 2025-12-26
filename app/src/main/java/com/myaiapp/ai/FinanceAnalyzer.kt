package com.myaiapp.ai

import com.myaiapp.data.local.model.*
import java.util.*
import kotlin.math.abs

/**
 * 财务分析器
 * 基于用户消费数据提供智能分析和建议
 */
class FinanceAnalyzer {

    /**
     * 分析结果
     */
    data class AnalysisResult(
        val summary: SpendingSummary,
        val insights: List<Insight>,
        val suggestions: List<Suggestion>,
        val healthScore: Int, // 0-100
        val trends: List<Trend>
    )

    /**
     * 消费摘要
     */
    data class SpendingSummary(
        val totalIncome: Double,
        val totalExpense: Double,
        val savingsRate: Double, // 储蓄率
        val topCategories: List<CategorySpending>,
        val dailyAverage: Double,
        val monthlyProjection: Double
    )

    /**
     * 分类消费
     */
    data class CategorySpending(
        val categoryName: String,
        val categoryIcon: String,
        val amount: Double,
        val percentage: Double,
        val trend: TrendDirection // 与上期相比
    )

    /**
     * 洞察
     */
    data class Insight(
        val type: InsightType,
        val title: String,
        val description: String,
        val importance: Importance,
        val icon: String
    )

    /**
     * 建议
     */
    data class Suggestion(
        val type: SuggestionType,
        val title: String,
        val description: String,
        val potentialSavings: Double?, // 潜在节省金额
        val priority: Priority
    )

    /**
     * 趋势
     */
    data class Trend(
        val period: String,
        val value: Double,
        val direction: TrendDirection
    )

    enum class InsightType {
        SPENDING_PATTERN, // 消费模式
        UNUSUAL_EXPENSE,  // 异常支出
        BUDGET_STATUS,    // 预算状态
        SAVINGS_GOAL,     // 储蓄目标
        CATEGORY_ANALYSIS // 分类分析
    }

    enum class SuggestionType {
        REDUCE_SPENDING,  // 减少支出
        INCREASE_SAVINGS, // 增加储蓄
        BUDGET_ADJUSTMENT,// 调整预算
        CATEGORY_LIMIT,   // 分类限制
        FINANCIAL_HABIT   // 理财习惯
    }

    enum class TrendDirection {
        UP, DOWN, STABLE
    }

    enum class Importance {
        HIGH, MEDIUM, LOW
    }

    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    /**
     * 分析财务数据
     */
    fun analyze(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        categories: List<Category>,
        daysToAnalyze: Int = 30
    ): AnalysisResult {
        val now = System.currentTimeMillis()
        val startTime = now - daysToAnalyze * 24 * 60 * 60 * 1000L
        val previousStartTime = startTime - daysToAnalyze * 24 * 60 * 60 * 1000L

        // 当前期间交易
        val currentTransactions = transactions.filter { it.date in startTime..now }
        // 上一期间交易
        val previousTransactions = transactions.filter { it.date in previousStartTime..startTime }

        // 计算摘要
        val summary = calculateSummary(currentTransactions, previousTransactions, categories, daysToAnalyze)

        // 生成洞察
        val insights = generateInsights(currentTransactions, previousTransactions, budgets, categories, summary)

        // 生成建议
        val suggestions = generateSuggestions(currentTransactions, budgets, categories, summary)

        // 计算健康分数
        val healthScore = calculateHealthScore(summary, budgets, currentTransactions)

        // 计算趋势
        val trends = calculateTrends(transactions, 6) // 最近6个月

        return AnalysisResult(
            summary = summary,
            insights = insights,
            suggestions = suggestions,
            healthScore = healthScore,
            trends = trends
        )
    }

    /**
     * 计算消费摘要
     */
    private fun calculateSummary(
        current: List<Transaction>,
        previous: List<Transaction>,
        categories: List<Category>,
        days: Int
    ): SpendingSummary {
        val totalIncome = current.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = current.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savingsRate = if (totalIncome > 0) (totalIncome - totalExpense) / totalIncome else 0.0

        // 按分类统计
        val categorySpending = current
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (categoryId, txns) ->
                val category = categories.find { it.id == categoryId }
                val amount = txns.sumOf { it.amount }
                val previousAmount = previous
                    .filter { it.categoryId == categoryId && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val trend = when {
                    amount > previousAmount * 1.1 -> TrendDirection.UP
                    amount < previousAmount * 0.9 -> TrendDirection.DOWN
                    else -> TrendDirection.STABLE
                }

                CategorySpending(
                    categoryName = category?.name ?: "未知",
                    categoryIcon = category?.icon ?: "more_horizontal",
                    amount = amount,
                    percentage = if (totalExpense > 0) amount / totalExpense else 0.0,
                    trend = trend
                )
            }
            .sortedByDescending { it.amount }
            .take(5)

        val dailyAverage = if (days > 0) totalExpense / days else 0.0
        val monthlyProjection = dailyAverage * 30

        return SpendingSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            savingsRate = savingsRate,
            topCategories = categorySpending,
            dailyAverage = dailyAverage,
            monthlyProjection = monthlyProjection
        )
    }

    /**
     * 生成洞察
     */
    private fun generateInsights(
        current: List<Transaction>,
        previous: List<Transaction>,
        budgets: List<Budget>,
        categories: List<Category>,
        summary: SpendingSummary
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // 消费模式分析
        val expenseChange = if (previous.isNotEmpty()) {
            val prevExpense = previous.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            if (prevExpense > 0) (summary.totalExpense - prevExpense) / prevExpense else 0.0
        } else 0.0

        if (abs(expenseChange) > 0.2) {
            insights.add(
                Insight(
                    type = InsightType.SPENDING_PATTERN,
                    title = if (expenseChange > 0) "支出上涨" else "支出下降",
                    description = "与上期相比，您的支出${if (expenseChange > 0) "增加" else "减少"}了${String.format("%.0f", abs(expenseChange) * 100)}%",
                    importance = if (expenseChange > 0) Importance.HIGH else Importance.LOW,
                    icon = if (expenseChange > 0) "trending_up" else "trending_down"
                )
            )
        }

        // 预算状态
        budgets.forEach { budget ->
            val spent = current
                .filter { it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val percentage = if (budget.amount > 0) spent / budget.amount else 0.0

            if (percentage > 0.9) {
                val category = categories.find { it.id == budget.categoryId }
                insights.add(
                    Insight(
                        type = InsightType.BUDGET_STATUS,
                        title = "${category?.name ?: "未知"}预算紧张",
                        description = if (percentage >= 1.0) {
                            "已超出预算${String.format("%.0f", (percentage - 1) * 100)}%"
                        } else {
                            "已使用${String.format("%.0f", percentage * 100)}%，剩余较少"
                        },
                        importance = if (percentage >= 1.0) Importance.HIGH else Importance.MEDIUM,
                        icon = "warning"
                    )
                )
            }
        }

        // 储蓄率分析
        if (summary.savingsRate < 0.1) {
            insights.add(
                Insight(
                    type = InsightType.SAVINGS_GOAL,
                    title = "储蓄率偏低",
                    description = "当前储蓄率仅${String.format("%.0f", summary.savingsRate * 100)}%，建议保持20%以上的储蓄",
                    importance = Importance.HIGH,
                    icon = "savings"
                )
            )
        } else if (summary.savingsRate > 0.3) {
            insights.add(
                Insight(
                    type = InsightType.SAVINGS_GOAL,
                    title = "储蓄表现优秀",
                    description = "储蓄率达到${String.format("%.0f", summary.savingsRate * 100)}%，继续保持！",
                    importance = Importance.LOW,
                    icon = "emoji_events"
                )
            )
        }

        // 异常支出检测
        if (current.isNotEmpty()) {
            val avgExpense = current
                .filter { it.type == TransactionType.EXPENSE }
                .map { it.amount }
                .average()

            current
                .filter { it.type == TransactionType.EXPENSE && it.amount > avgExpense * 3 }
                .take(3)
                .forEach { txn ->
                    val category = categories.find { it.id == txn.categoryId }
                    insights.add(
                        Insight(
                            type = InsightType.UNUSUAL_EXPENSE,
                            title = "大额支出提醒",
                            description = "${category?.name ?: "未知"}: ¥${String.format("%.2f", txn.amount)}",
                            importance = Importance.MEDIUM,
                            icon = "priority_high"
                        )
                    )
                }
        }

        return insights.sortedBy { it.importance.ordinal }
    }

    /**
     * 生成建议
     */
    private fun generateSuggestions(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        categories: List<Category>,
        summary: SpendingSummary
    ): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()

        // 分析高消费分类
        summary.topCategories.take(3).forEach { categorySpending ->
            if (categorySpending.trend == TrendDirection.UP && categorySpending.percentage > 0.2) {
                val potentialSavings = categorySpending.amount * 0.2
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.REDUCE_SPENDING,
                        title = "控制${categorySpending.categoryName}支出",
                        description = "该分类占比${String.format("%.0f", categorySpending.percentage * 100)}%且持续上涨，建议设置预算",
                        potentialSavings = potentialSavings,
                        priority = Priority.HIGH
                    )
                )
            }
        }

        // 储蓄建议
        if (summary.savingsRate < 0.2 && summary.totalIncome > 0) {
            val targetSavings = summary.totalIncome * 0.2
            val currentSavings = summary.totalIncome - summary.totalExpense
            val needToSave = targetSavings - currentSavings

            if (needToSave > 0) {
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.INCREASE_SAVINGS,
                        title = "提升储蓄率",
                        description = "建议每月额外储蓄¥${String.format("%.0f", needToSave)}以达到20%储蓄率",
                        potentialSavings = needToSave,
                        priority = Priority.MEDIUM
                    )
                )
            }
        }

        // 预算调整建议
        val categoriesWithoutBudget = summary.topCategories
            .filter { spending -> budgets.none { it.categoryId == categories.find { c -> c.name == spending.categoryName }?.id } }

        categoriesWithoutBudget.take(2).forEach { spending ->
            suggestions.add(
                Suggestion(
                    type = SuggestionType.BUDGET_ADJUSTMENT,
                    title = "为${spending.categoryName}设置预算",
                    description = "建议预算金额: ¥${String.format("%.0f", spending.amount * 0.9)}",
                    potentialSavings = spending.amount * 0.1,
                    priority = Priority.MEDIUM
                )
            )
        }

        // 理财习惯建议
        val weekendTransactions = transactions.filter { txn ->
            val cal = Calendar.getInstance().apply { timeInMillis = txn.date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) && txn.type == TransactionType.EXPENSE
        }

        val weekdayTransactions = transactions.filter { txn ->
            val cal = Calendar.getInstance().apply { timeInMillis = txn.date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            dayOfWeek !in listOf(Calendar.SATURDAY, Calendar.SUNDAY) && txn.type == TransactionType.EXPENSE
        }

        if (weekendTransactions.isNotEmpty() && weekdayTransactions.isNotEmpty()) {
            val weekendAvg = weekendTransactions.sumOf { it.amount } / 2 // 假设分析周期为1周
            val weekdayAvg = weekdayTransactions.sumOf { it.amount } / 5

            if (weekendAvg > weekdayAvg * 2) {
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.FINANCIAL_HABIT,
                        title = "注意周末消费",
                        description = "您周末的日均消费是工作日的${String.format("%.1f", weekendAvg / weekdayAvg)}倍",
                        potentialSavings = null,
                        priority = Priority.LOW
                    )
                )
            }
        }

        return suggestions.sortedBy { it.priority.ordinal }
    }

    /**
     * 计算财务健康分数
     */
    private fun calculateHealthScore(
        summary: SpendingSummary,
        budgets: List<Budget>,
        transactions: List<Transaction>
    ): Int {
        var score = 100

        // 储蓄率影响 (-30 ~ 0)
        when {
            summary.savingsRate < 0 -> score -= 30
            summary.savingsRate < 0.1 -> score -= 20
            summary.savingsRate < 0.2 -> score -= 10
        }

        // 预算执行情况 (-20 ~ 0)
        val budgetOverruns = budgets.count { budget ->
            val spent = transactions
                .filter { it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            spent > budget.amount
        }
        score -= budgetOverruns * 5

        // 消费集中度 (-10 ~ 0)
        val topCategoryPercentage = summary.topCategories.firstOrNull()?.percentage ?: 0.0
        if (topCategoryPercentage > 0.5) {
            score -= 10
        }

        // 收支平衡 (-20 ~ 0)
        if (summary.totalExpense > summary.totalIncome) {
            score -= 20
        }

        return score.coerceIn(0, 100)
    }

    /**
     * 计算趋势
     */
    private fun calculateTrends(transactions: List<Transaction>, months: Int): List<Trend> {
        val cal = Calendar.getInstance()
        val trends = mutableListOf<Trend>()

        var previousAmount = 0.0

        for (i in months - 1 downTo 0) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -i)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            val monthStart = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            val monthEnd = cal.timeInMillis

            val monthExpense = transactions
                .filter { it.date in monthStart..monthEnd && it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val direction = when {
                previousAmount == 0.0 -> TrendDirection.STABLE
                monthExpense > previousAmount * 1.1 -> TrendDirection.UP
                monthExpense < previousAmount * 0.9 -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }

            trends.add(
                Trend(
                    period = "${cal.get(Calendar.MONTH) + 1}月",
                    value = monthExpense,
                    direction = direction
                )
            )

            previousAmount = monthExpense
        }

        return trends
    }
}
