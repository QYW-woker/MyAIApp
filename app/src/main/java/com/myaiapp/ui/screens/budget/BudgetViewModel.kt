package com.myaiapp.ui.screens.budget

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

data class BudgetUiState(
    val isLoading: Boolean = true,
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgetSpentMap: Map<String, Double> = emptyMap()
)

class BudgetViewModel(private val context: Context) : ViewModel() {

    private val storageManager = FileStorageManager(context)

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val budgets = storageManager.getBudgets()
                val categories = storageManager.getCategories()
                val bookId = storageManager.getCurrentBookId()
                val transactions = storageManager.getTransactions(bookId)

                // 获取本月时间范围
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val monthStart = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val monthEnd = cal.timeInMillis

                // 本月交易
                val monthTransactions = transactions.filter {
                    it.date in monthStart until monthEnd && it.type == TransactionType.EXPENSE
                }

                // 计算各分类已花费
                val spentByCategory = monthTransactions
                    .groupBy { it.categoryId }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }

                // 计算各预算已花费金额
                val budgetSpentMap = budgets.associate { budget ->
                    val spent = if (budget.categoryId != null) {
                        spentByCategory[budget.categoryId] ?: 0.0
                    } else {
                        monthTransactions.sumOf { it.amount }
                    }
                    budget.id to spent
                }

                val totalSpent = monthTransactions.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        budgets = budgets,
                        categories = categories,
                        totalBudget = budgets.sumOf { b -> b.amount },
                        totalSpent = totalSpent,
                        budgetSpentMap = budgetSpentMap
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                val currentBudgets = _uiState.value.budgets.toMutableList()
                val existingIndex = currentBudgets.indexOfFirst { it.id == budget.id }

                if (existingIndex >= 0) {
                    currentBudgets[existingIndex] = budget
                } else {
                    currentBudgets.add(budget)
                }

                storageManager.saveBudgets(currentBudgets)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                val currentBudgets = _uiState.value.budgets.filter { it.id != budget.id }
                storageManager.saveBudgets(currentBudgets)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

class BudgetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetViewModel(context.applicationContext) as T
    }
}
