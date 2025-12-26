package com.myaiapp.ui.screens.analysis

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.ai.FinanceAnalyzer
import com.myaiapp.data.local.FileStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val analysisResult: FinanceAnalyzer.AnalysisResult? = null,
    val error: String? = null
)

class AnalysisViewModel(
    private val context: Context
) : ViewModel() {

    private val storageManager = FileStorageManager(context)
    private val analyzer = FinanceAnalyzer()

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAndAnalyze()
    }

    private fun loadAndAnalyze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val bookId = storageManager.getCurrentBookId()
                val transactions = storageManager.getTransactions(bookId)
                val budgets = storageManager.getBudgets()
                val categories = storageManager.getCategories()

                // 需要至少5笔交易才能进行有意义的分析
                if (transactions.size < 5) {
                    _uiState.update { it.copy(isLoading = false, analysisResult = null) }
                    return@launch
                }

                val result = analyzer.analyze(
                    transactions = transactions,
                    budgets = budgets,
                    categories = categories,
                    daysToAnalyze = 30
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        analysisResult = result
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        loadAndAnalyze()
    }
}

class AnalysisViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalysisViewModel(context.applicationContext) as T
    }
}
