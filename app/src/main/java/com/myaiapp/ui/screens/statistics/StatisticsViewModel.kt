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
    val percentage: Double,
    val count: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val periodType: String = "month", // week, month, year
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryStats: List<CategoryStatistics> = emptyList(),
    val periodLabel: String = ""
)

class StatisticsViewModel(private val context: Context) : ViewModel() {

    private val storageManager = FileStorageManager(context)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setPeriodType(type: String) {
        _uiState.update { it.copy(periodType = type) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bookId = storageManager.getCurrentBookId()
                val transactions = storageManager.getTransactions(bookId)
                val categories = storageManager.getCategories()

                val (startTime, endTime, periodLabel) = getPeriodRange(_uiState.value.periodType)

                val periodTransactions = transactions.filter { it.date in startTime..endTime }

                val totalIncome = periodTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val totalExpense = periodTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                // 按分类统计支出
                val expenseByCategory = periodTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val category = categories.find { it.id == categoryId }
                        CategoryStatistics(
                            categoryId = categoryId,
                            categoryName = category?.name ?: "未知",
                            categoryIcon = category?.icon ?: "more_horizontal",
                            categoryColor = category?.color ?: "#808080",
                            amount = txns.sumOf { it.amount },
                            percentage = if (totalExpense > 0) txns.sumOf { it.amount } / totalExpense else 0.0,
                            count = txns.size
                        )
                    }
                    .sortedByDescending { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        balance = totalIncome - totalExpense,
                        categoryStats = expenseByCategory,
                        periodLabel = periodLabel
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun getPeriodRange(type: String): Triple<Long, Long, String> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis

        return when (type) {
            "week" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                Triple(cal.timeInMillis, end, "近7天")
            }
            "year" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                Triple(cal.timeInMillis, end, "${cal.get(Calendar.YEAR)}年")
            }
            else -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                Triple(cal.timeInMillis, end, "${cal.get(Calendar.MONTH) + 1}月")
            }
        }
    }
}

class StatisticsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(context.applicationContext) as T
    }
}
