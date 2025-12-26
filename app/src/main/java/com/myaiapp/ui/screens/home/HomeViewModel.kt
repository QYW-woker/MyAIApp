package com.myaiapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.*
import com.myaiapp.util.getCurrentMonthStartTimestamp
import com.myaiapp.util.formatMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetProgressData(
    val name: String,
    val spent: Double,
    val total: Double
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentBookName: String = "默认账本",
    val monthLabel: String = "",
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthBalance: Double = 0.0,
    val budgetProgress: BudgetProgressData? = null,
    val savingsPlans: List<SavingsPlan> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList()
)

class HomeViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 获取当前账本
                val currentBookId = storageManager.getCurrentBookId()
                val books = storageManager.getAccountBooks()
                val currentBook = books.find { it.id == currentBookId } ?: books.firstOrNull()

                // 获取分类
                val categories = storageManager.getCategories()

                // 获取本月交易
                val monthStart = getCurrentMonthStartTimestamp()
                val allTransactions = currentBook?.let {
                    storageManager.getTransactions(it.id)
                } ?: emptyList()

                val monthTransactions = allTransactions.filter { it.date >= monthStart }
                val monthIncome = monthTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val monthExpense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                // 获取预算
                val budgets = storageManager.getBudgets()
                val monthlyBudget = budgets.find {
                    it.period == BudgetPeriod.MONTHLY && it.type == BudgetType.TOTAL
                }
                val budgetProgress = monthlyBudget?.let {
                    BudgetProgressData(
                        name = it.name,
                        spent = monthExpense,
                        total = it.amount
                    )
                }

                // 获取存钱计划
                val savingsPlans = storageManager.getSavingsPlans()
                    .filter { it.currentAmount < it.targetAmount }

                // 获取最近记录（最近7天或20条）
                val recentTransactions = allTransactions
                    .sortedByDescending { it.date }
                    .take(20)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentBookName = currentBook?.name ?: "默认账本",
                        monthLabel = formatMonth(System.currentTimeMillis()),
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        monthBalance = monthIncome - monthExpense,
                        budgetProgress = budgetProgress,
                        savingsPlans = savingsPlans,
                        recentTransactions = recentTransactions,
                        categories = categories
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
