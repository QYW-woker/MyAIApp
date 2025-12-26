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
import java.util.UUID

data class SavingsUiState(
    val isLoading: Boolean = true,
    val plans: List<SavingsPlan> = emptyList(),
    val records: Map<String, List<SavingsRecord>> = emptyMap(),
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

                // 加载每个计划的记录
                val recordsByPlan = mutableMapOf<String, List<SavingsRecord>>()
                plans.forEach { plan ->
                    val records = storageManager.getSavingsRecords(plan.id)
                    recordsByPlan[plan.id] = records
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        plans = plans,
                        records = recordsByPlan,
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

    fun savePlan(plan: SavingsPlan) {
        viewModelScope.launch {
            try {
                val currentPlans = _uiState.value.plans.toMutableList()
                val existingIndex = currentPlans.indexOfFirst { it.id == plan.id }

                if (existingIndex >= 0) {
                    currentPlans[existingIndex] = plan
                } else {
                    currentPlans.add(plan)
                }

                storageManager.saveSavingsPlans(currentPlans)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePlan(plan: SavingsPlan) {
        viewModelScope.launch {
            try {
                val currentPlans = _uiState.value.plans.filter { it.id != plan.id }
                storageManager.saveSavingsPlans(currentPlans)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deposit(planId: String, amount: Double, note: String) {
        viewModelScope.launch {
            try {
                // 创建存入记录
                val record = SavingsRecord(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = note.ifEmpty { null }
                )

                // 使用 addSavingsRecord 方法（会同时更新计划金额）
                storageManager.addSavingsRecord(planId, record)

                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun withdraw(planId: String, amount: Double, note: String) {
        viewModelScope.launch {
            try {
                // 创建取出记录（负数表示取出）
                val record = SavingsRecord(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    amount = -amount,
                    date = System.currentTimeMillis(),
                    note = note.ifEmpty { null }
                )

                // 使用 addSavingsRecord 方法（会同时更新计划金额）
                storageManager.addSavingsRecord(planId, record)

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

class SavingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavingsViewModel(context.applicationContext) as T
    }
}
