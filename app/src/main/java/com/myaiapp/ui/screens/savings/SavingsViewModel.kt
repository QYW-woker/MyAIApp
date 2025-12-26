package com.myaiapp.ui.screens.savings

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

data class SavingsUiState(
    val isLoading: Boolean = true,
    val plans: List<SavingsPlan> = emptyList(),
    val totalTarget: Double = 0.0,
    val totalSaved: Double = 0.0
)

class SavingsViewModel(private val context: Context) : ViewModel() {

    private val storageManager = FileStorageManager(context)

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val plans = storageManager.getSavingsPlans()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        plans = plans,
                        totalTarget = plans.sumOf { p -> p.targetAmount },
                        totalSaved = plans.sumOf { p -> p.currentAmount }
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

class SavingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavingsViewModel(context.applicationContext) as T
    }
}
