package com.myaiapp.ui.screens.records

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.util.getCurrentMonthStartTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordsUiState(
    val isLoading: Boolean = true,
    val filterIndex: Int = 0,  // 0: 全部, 1: 支出, 2: 收入
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val periodIncome: Double = 0.0,
    val periodExpense: Double = 0.0,
    val periodBalance: Double = 0.0
)

class RecordsViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentBookId = storageManager.getCurrentBookId()
                val categories = storageManager.getCategories()

                allTransactions = storageManager.getTransactions(currentBookId)
                    .sortedByDescending { it.date }

                applyFilter(_uiState.value.filterIndex, categories)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilter(filterIndex: Int) {
        viewModelScope.launch {
            val categories = storageManager.getCategories()
            applyFilter(filterIndex, categories)
        }
    }

    private fun applyFilter(filterIndex: Int, categories: List<Category>) {
        val monthStart = getCurrentMonthStartTimestamp()
        val monthTransactions = allTransactions.filter { it.date >= monthStart }

        val filteredTransactions = when (filterIndex) {
            1 -> monthTransactions.filter { it.type == TransactionType.EXPENSE }
            2 -> monthTransactions.filter { it.type == TransactionType.INCOME }
            else -> monthTransactions
        }

        val periodIncome = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val periodExpense = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        _uiState.update {
            it.copy(
                isLoading = false,
                filterIndex = filterIndex,
                transactions = filteredTransactions,
                categories = categories,
                periodIncome = periodIncome,
                periodExpense = periodExpense,
                periodBalance = periodIncome - periodExpense
            )
        }
    }
}

class RecordsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
