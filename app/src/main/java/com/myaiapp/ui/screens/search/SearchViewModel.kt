package com.myaiapp.ui.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Transaction> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val filterType: String? = null // "expense", "income", null
)

class SearchViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            val history = storageManager.getSearchHistory()
            _uiState.update { it.copy(searchHistory = history) }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }

        // 取消之前的搜索
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 防抖：等待用户停止输入
            delay(300)

            try {
                val bookId = storageManager.getCurrentBookId()
                val allTransactions = storageManager.getTransactions(bookId)
                val categories = storageManager.getCategories()
                val accounts = storageManager.getAssetAccounts()

                // 填充分类和账户信息
                val enrichedTransactions = allTransactions.map { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    val account = accounts.find { it.id == transaction.accountId }
                    transaction.copy(
                        categoryName = category?.name,
                        categoryIcon = category?.icon,
                        categoryColor = category?.color,
                        accountName = account?.name
                    )
                }

                val filteredResults = enrichedTransactions.filter { transaction ->
                    val matchesQuery = transaction.note?.contains(query, ignoreCase = true) == true ||
                            transaction.categoryName?.contains(query, ignoreCase = true) == true ||
                            transaction.amount.toString().contains(query)

                    val matchesType = when (_uiState.value.filterType) {
                        "expense" -> transaction.type == TransactionType.EXPENSE
                        "income" -> transaction.type == TransactionType.INCOME
                        else -> true
                    }

                    matchesQuery && matchesType
                }.sortedByDescending { it.date }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = filteredResults
                    )
                }

                // 保存搜索历史
                if (query.length >= 2) {
                    saveSearchHistory(query)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilterType(type: String?) {
        _uiState.update { it.copy(filterType = type) }
        // 重新搜索
        if (_uiState.value.query.isNotBlank()) {
            search(_uiState.value.query)
        }
    }

    private fun saveSearchHistory(query: String) {
        viewModelScope.launch {
            val currentHistory = _uiState.value.searchHistory.toMutableList()
            // 移除重复项
            currentHistory.remove(query)
            // 添加到开头
            currentHistory.add(0, query)
            // 只保留最近10条
            val newHistory = currentHistory.take(10)
            storageManager.saveSearchHistory(newHistory)
            _uiState.update { it.copy(searchHistory = newHistory) }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            storageManager.saveSearchHistory(emptyList())
            _uiState.update { it.copy(searchHistory = emptyList()) }
        }
    }
}

class SearchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
