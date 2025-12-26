package com.myaiapp.ui.screens.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryStatistics(
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
    val periodIndex: Int = 1, // 0: 周, 1: 月, 2: 年
    val typeIndex: Int = 0, // 0: 支出, 1: 收入
    val periodLabel: String = "",
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val dailyAverage: Double = 0.0,
    val previousPeriodAmount: Double = 0.0,
    val categoryStats: List<CategoryStatistics> = emptyList()
)

class StatisticsViewModel(private val context: Context) : ViewModel() {

    private val storageManager = FileStorageManager(context)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var currentCalendar = Calendar.getInstance()

    init {
        loadData()
    }

    fun setPeriod(index: Int) {
        _uiState.update { it.copy(periodIndex = index) }
        currentCalendar = Calendar.getInstance()
        loadData()
    }

    fun setType(index: Int) {
        _uiState.update { it.copy(typeIndex = index) }
        loadData()
    }

    fun previousPeriod() {
        when (_uiState.value.periodIndex) {
            0 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            1 -> currentCalendar.add(Calendar.MONTH, -1)
            2 -> currentCalendar.add(Calendar.YEAR, -1)
        }
        loadData()
    }

    fun nextPeriod() {
        when (_uiState.value.periodIndex) {
            0 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            1 -> currentCalendar.add(Calendar.MONTH, 1)
            2 -> currentCalendar.add(Calendar.YEAR, 1)
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bookId = storageManager.getCurrentBookId()
                val transactions = storageManager.getTransactions(bookId)
                val categories = storageManager.getCategories()

                val (startTime, endTime, periodLabel, daysInPeriod) = getPeriodRange()

                val transactionType = if (_uiState.value.typeIndex == 0)
                    TransactionType.EXPENSE else TransactionType.INCOME

                val periodTransactions = transactions.filter {
                    it.date in startTime..endTime && it.type == transactionType
                }

                val totalAmount = periodTransactions.sumOf { it.amount }
                val transactionCount = periodTransactions.size
                val dailyAverage = if (daysInPeriod > 0) totalAmount / daysInPeriod else 0.0

                // 按分类统计
                val categoryStats = periodTransactions
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val category = categories.find { it.id == categoryId }
                        val amount = txns.sumOf { it.amount }
                        CategoryStatistics(
                            categoryId = categoryId,
                            categoryName = category?.name ?: "未知",
                            categoryIcon = category?.icon ?: "more_horizontal",
                            categoryColor = category?.color ?: "#808080",
                            amount = amount,
                            percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f,
                            count = txns.size
                        )
                    }
                    .sortedByDescending { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        periodLabel = periodLabel,
                        totalAmount = totalAmount,
                        transactionCount = transactionCount,
                        dailyAverage = dailyAverage,
                        previousPeriodAmount = 0.0,
                        categoryStats = categoryStats
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun getPeriodRange(): PeriodInfo {
        val cal = currentCalendar.clone() as Calendar
        val end: Long
        val start: Long
        val label: String
        val days: Int

        when (_uiState.value.periodIndex) {
            0 -> { // 周
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                end = cal.timeInMillis
                label = "第${cal.get(Calendar.WEEK_OF_YEAR)}周"
                days = 7
            }
            2 -> { // 年
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                start = cal.timeInMillis
                cal.set(Calendar.MONTH, 11)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                end = cal.timeInMillis
                label = "${cal.get(Calendar.YEAR)}年"
                days = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
            }
            else -> { // 月
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                end = cal.timeInMillis
                label = "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月"
                days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
        }

        return PeriodInfo(start, end, label, days)
    }

    private data class PeriodInfo(
        val startTime: Long,
        val endTime: Long,
        val label: String,
        val daysInPeriod: Int
    )
}

class StatisticsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(context.applicationContext) as T
    }
}
