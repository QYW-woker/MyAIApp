package com.myaiapp.ui.screens.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.*
import com.myaiapp.util.getCurrentMonthStartTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BudgetCategory(
    val id: String,
    val name: String,
    val icon: String,
    val color: String
)

data class BudgetItemData(
    val id: String,
    val name: String,
    val categoryId: String?,
    val categoryName: String?,
    val categoryIcon: String,
    val categoryColor: String,
    val budget: Double,
    val spent: Double
)

data class BudgetUiState(
    val isLoading: Boolean = true,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val remaining: Double = 0.0,
    val budgets: List<BudgetItemData> = emptyList(),
    val categories: List<BudgetCategory> = emptyList()
)

class BudgetViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val bookId = storageManager.getCurrentBookId()
                val budgets = storageManager.getBudgets().filter { it.bookId == bookId }
                val categories = storageManager.getCategories()
                val transactions = storageManager.getTransactions(bookId)

                val monthStart = getCurrentMonthStartTimestamp()
                val monthTransactions = transactions.filter {
                    it.date >= monthStart && it.type == TransactionType.EXPENSE
                }

                // 计算每个预算的花费
                val budgetItems = budgets.map { budget ->
                    val category = categories.find { it.id == budget.categoryId }
                    val spent = if (budget.type == BudgetType.TOTAL) {
                        monthTransactions.sumOf { it.amount }
                    } else {
                        monthTransactions
                            .filter { it.categoryId == budget.categoryId }
                            .sumOf { it.amount }
                    }

                    BudgetItemData(
                        id = budget.id,
                        name = budget.name,
                        categoryId = budget.categoryId,
                        categoryName = category?.name,
                        categoryIcon = category?.icon ?: "more_horizontal",
                        categoryColor = category?.color ?: "#5B8DEF",
                        budget = budget.amount,
                        spent = spent
                    )
                }

                val totalBudget = budgets.sumOf { it.amount }
                val totalSpent = monthTransactions.sumOf { it.amount }

                val expenseCategories = categories
                    .filter { it.type == TransactionType.EXPENSE }
                    .map { BudgetCategory(it.id, it.name, it.icon, it.color) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalBudget = totalBudget,
                        totalSpent = totalSpent,
                        remaining = totalBudget - totalSpent,
                        budgets = budgetItems,
                        categories = expenseCategories
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addBudget(name: String, categoryId: String?, amount: Double) {
        viewModelScope.launch {
            try {
                val bookId = storageManager.getCurrentBookId()
                val budget = Budget(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    type = if (categoryId == null) BudgetType.TOTAL else BudgetType.CATEGORY,
                    categoryId = categoryId,
                    amount = amount,
                    period = BudgetPeriod.MONTHLY,
                    startDate = System.currentTimeMillis(),
                    bookId = bookId
                )
                storageManager.addBudget(budget)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                storageManager.deleteBudget(budgetId)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class BudgetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
