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
import java.util.UUID

data class AssetsUiState(
    val isLoading: Boolean = true,
    val assetAccounts: List<AssetAccount> = emptyList(),
    val liabilityAccounts: List<AssetAccount> = emptyList(),
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

                // 分类：资产账户和负债账户
                val assetTypes = listOf(
                    AssetType.CASH, AssetType.DEBIT_CARD, AssetType.ALIPAY,
                    AssetType.WECHAT, AssetType.INVESTMENT, AssetType.RECEIVABLE
                )
                val liabilityTypes = listOf(
                    AssetType.CREDIT_CARD, AssetType.PAYABLE
                )

                val assetAccounts = accounts.filter { it.type in assetTypes && !it.isArchived }
                val liabilityAccounts = accounts.filter { it.type in liabilityTypes && !it.isArchived }

                val totalAssets = assetAccounts.sumOf { it.balance.coerceAtLeast(0.0) }
                val totalLiabilities = liabilityAccounts.sumOf { (-it.balance).coerceAtLeast(0.0) } +
                        assetAccounts.sumOf { (-it.balance).coerceAtLeast(0.0) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        assetAccounts = assetAccounts,
                        liabilityAccounts = liabilityAccounts,
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

    fun addAccount(name: String, type: AssetType, balance: Double, icon: String, color: String) {
        viewModelScope.launch {
            val account = AssetAccount(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                balance = balance,
                icon = icon,
                color = color
            )
            storageManager.addAssetAccount(account)
            loadData()
        }
    }

    fun updateAccount(account: AssetAccount) {
        viewModelScope.launch {
            storageManager.updateAssetAccount(account)
            loadData()
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            storageManager.deleteAssetAccount(accountId)
            loadData()
        }
    }
}

class AssetsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AssetsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
