package com.myaiapp.ui.screens.assets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.AssetAccount
import com.myaiapp.data.local.model.AssetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssetsUiState(
    val isLoading: Boolean = true,
    val accounts: List<AssetAccount> = emptyList(),
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netWorth: Double = 0.0
)

class AssetsViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val accounts = storageManager.getAssetAccounts()
                    .filter { !it.isArchived && it.includeInTotal }
                    .sortedBy { it.type.ordinal }

                // 计算资产和负债
                var totalAssets = 0.0
                var totalLiabilities = 0.0

                accounts.forEach { account ->
                    when (account.type) {
                        AssetType.CREDIT_CARD -> {
                            // 信用卡余额为负表示欠款
                            if (account.balance < 0) {
                                totalLiabilities += -account.balance
                            } else {
                                totalAssets += account.balance
                            }
                        }
                        AssetType.PAYABLE -> {
                            totalLiabilities += account.balance
                        }
                        AssetType.RECEIVABLE -> {
                            totalAssets += account.balance
                        }
                        else -> {
                            if (account.balance >= 0) {
                                totalAssets += account.balance
                            } else {
                                totalLiabilities += -account.balance
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accounts = accounts,
                        totalAssets = totalAssets,
                        totalLiabilities = totalLiabilities,
                        netWorth = totalAssets - totalLiabilities
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveAccount(account: AssetAccount) {
        viewModelScope.launch {
            storageManager.saveAssetAccount(account)
            loadData()
        }
    }

    fun deleteAccount(account: AssetAccount) {
        viewModelScope.launch {
            storageManager.deleteAssetAccount(account.id)
            loadData()
        }
    }

    fun transfer(fromId: String, toId: String, amount: Double, note: String) {
        viewModelScope.launch {
            val accounts = storageManager.getAssetAccounts()
            val fromAccount = accounts.find { it.id == fromId }
            val toAccount = accounts.find { it.id == toId }

            if (fromAccount != null && toAccount != null) {
                // 更新转出账户余额
                val updatedFrom = fromAccount.copy(
                    balance = fromAccount.balance - amount,
                    updatedAt = System.currentTimeMillis()
                )
                storageManager.saveAssetAccount(updatedFrom)

                // 更新转入账户余额
                val updatedTo = toAccount.copy(
                    balance = toAccount.balance + amount,
                    updatedAt = System.currentTimeMillis()
                )
                storageManager.saveAssetAccount(updatedTo)

                loadData()
            }
        }
    }
}

class AssetsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AssetsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
