package com.myaiapp.ui.screens.savings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.SavingsDeposit
import com.myaiapp.data.local.model.SavingsPlan
import com.myaiapp.data.local.model.SavingsRecord
import com.myaiapp.data.local.model.SavingsType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SavingsPlanData(
    val id: String,
    val plan: SavingsPlan,
    val records: List<SavingsRecord> = emptyList()
)

data class SavingsUiState(
    val isLoading: Boolean = true,
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val activePlansCount: Int = 0,
    val plans: List<SavingsPlanData> = emptyList()
)

class SavingsViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val plans = storageManager.getSavingsPlans()
                    .sortedByDescending { it.createdAt }

                val planDataList = plans.map { plan ->
                    val records = storageManager.getSavingsRecords(plan.id)
                        .sortedByDescending { it.date }
                    SavingsPlanData(
                        id = plan.id,
                        plan = plan,
                        records = records
                    )
                }

                val activePlans = plans.filter { it.currentAmount < it.targetAmount }
                val totalSaved = plans.sumOf { it.currentAmount }
                val totalTarget = activePlans.sumOf { it.targetAmount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalSaved = totalSaved,
                        totalTarget = totalTarget,
                        activePlansCount = activePlans.size,
                        plans = planDataList
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addPlan(name: String, emoji: String, targetAmount: Double) {
        viewModelScope.launch {
            try {
                val plan = SavingsPlan(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = emoji,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    type = SavingsType.FLEXIBLE
                )
                storageManager.addSavingsPlan(plan)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addDeposit(planId: String, amount: Double, note: String) {
        viewModelScope.launch {
            try {
                val deposit = SavingsDeposit(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = note
                )
                storageManager.addDepositToPlan(planId, deposit)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePlan(plan: SavingsPlan) {
        viewModelScope.launch {
            try {
                storageManager.deleteSavingsPlan(plan.id)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun savePlan(plan: SavingsPlan) {
        viewModelScope.launch {
            try {
                storageManager.saveSavingsPlan(plan)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deposit(planId: String, amount: Double, note: String) {
        viewModelScope.launch {
            try {
                val record = SavingsRecord(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = note.takeIf { it.isNotBlank() }
                )
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
                val record = SavingsRecord(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    amount = -amount, // 负数表示取出
                    date = System.currentTimeMillis(),
                    note = note.takeIf { it.isNotBlank() }
                )
                storageManager.addSavingsRecord(planId, record)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SavingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavingsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
