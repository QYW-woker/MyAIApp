package com.myaiapp.ui.screens.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class CategoryStat(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val periodIndex: Int = 1,  // 0: 周, 1: 月, 2: 年
    val typeIndex: Int = 0,    // 0: 支出, 1: 收入
    val periodLabel: String = "",
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val dailyAverage: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList()
)

class StatisticsViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var categories: List<Category> = emptyList()
    private var currentCalendar = Calendar.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val bookId = storageManager.getCurrentBookId()
                allTransactions = storageManager.getTransactions(bookId)
                categories = storageManager.getCategories()

                updateStats()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setPeriod(index: Int) {
        _uiState.update { it.copy(periodIndex = index) }
        currentCalendar = Calendar.getInstance()
        updateStats()
    }

    fun setType(index: Int) {
        _uiState.update { it.copy(typeIndex = index) }
        updateStats()
    }

    fun previousPeriod() {
        when (_uiState.value.periodIndex) {
            0 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            1 -> currentCalendar.add(Calendar.MONTH, -1)
            2 -> currentCalendar.add(Calendar.YEAR, -1)
        }
        updateStats()
    }

    fun nextPeriod() {
        when (_uiState.value.periodIndex) {
            0 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            1 -> currentCalendar.add(Calendar.MONTH, 1)
            2 -> currentCalendar.add(Calendar.YEAR, 1)
        }
        updateStats()
    }

    private fun updateStats() {
        val state = _uiState.value
        val (startTime, endTime, label, days) = getPeriodRange(state.periodIndex)

        val type = if (state.typeIndex == 0) TransactionType.EXPENSE else TransactionType.INCOME

        val periodTransactions = allTransactions.filter {
            it.date in startTime..endTime && it.type == type
        }

        val totalAmount = periodTransactions.sumOf { it.amount }
        val transactionCount = periodTransactions.size
        val dailyAverage = if (days > 0) totalAmount / days else 0.0

        // 按分类统计
        val categoryAmounts = periodTransactions.groupBy { it.categoryId }
        val categoryStats = categoryAmounts.map { (categoryId, transactions) ->
            val category = categories.find { it.id == categoryId }
            val amount = transactions.sumOf { it.amount }
            CategoryStat(
                categoryId = categoryId,
                categoryName = category?.name ?: "未知",
                categoryIcon = category?.icon ?: "more_horizontal",
                categoryColor = category?.color ?: "#A3A3A3",
                amount = amount,
                percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f,
                count = transactions.size
            )
        }.sortedByDescending { it.amount }

        _uiState.update {
            it.copy(
                isLoading = false,
                periodLabel = label,
                totalAmount = totalAmount,
                transactionCount = transactionCount,
                dailyAverage = dailyAverage,
                categoryStats = categoryStats
            )
        }
    }

    private fun getPeriodRange(periodIndex: Int): PeriodInfo {
        val cal = currentCalendar.clone() as Calendar

        return when (periodIndex) {
            0 -> { // 周
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis

                val label = formatDate(start) + " - " + formatDate(end)
                PeriodInfo(start, end, label, 7)
            }
            1 -> { // 月
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis

                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, daysInMonth)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis

                val label = formatMonth(start)
                PeriodInfo(start, end, label, daysInMonth)
            }
            else -> { // 年
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis

                val label = "${cal.get(Calendar.YEAR)}年"
                PeriodInfo(start, end, label, 365)
            }
        }
    }
}

private data class PeriodInfo(
    val startTime: Long,
    val endTime: Long,
    val label: String,
    val days: Int
)

class StatisticsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
