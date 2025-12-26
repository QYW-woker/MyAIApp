package com.myaiapp.ui.screens.record

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

data class RecordUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val selectedCategoryId: String? = null,
    val selectedAccountId: String? = null,
    val selectedAccountName: String = "现金",
    val toAccountId: String? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val accounts: List<AssetAccount> = emptyList(),
    val bookId: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = amount.isNotEmpty() &&
                amount.toDoubleOrNull()?.let { it > 0 } == true &&
                selectedCategoryId != null &&
                selectedAccountId != null
}

class RecordViewModel(
    private val storageManager: FileStorageManager,
    private val transactionId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var originalTransaction: Transaction? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val categories = storageManager.getCategories()
                val accounts = storageManager.getAssetAccounts()
                val bookId = storageManager.getCurrentBookId()

                val defaultAccount = accounts.firstOrNull()
                var state = _uiState.value.copy(
                    isLoading = false,
                    categories = categories,
                    accounts = accounts,
                    bookId = bookId,
                    selectedAccountId = defaultAccount?.id,
                    selectedAccountName = defaultAccount?.name ?: "现金"
                )

                // 如果是编辑模式，加载现有交易
                if (transactionId != null) {
                    val transactions = storageManager.getTransactions(bookId)
                    val transaction = transactions.find { it.id == transactionId }
                    if (transaction != null) {
                        originalTransaction = transaction
                        val account = accounts.find { it.id == transaction.accountId }
                        state = state.copy(
                            isEditing = true,
                            transactionType = transaction.type,
                            amount = transaction.amount.toString(),
                            selectedCategoryId = transaction.categoryId,
                            selectedAccountId = transaction.accountId,
                            selectedAccountName = account?.name ?: "现金",
                            toAccountId = transaction.toAccountId,
                            note = transaction.note,
                            date = transaction.date,
                            tags = transaction.tags
                        )
                    }
                } else {
                    // 新建时默认选择第一个支出分类
                    val defaultCategory = categories.find { it.type == TransactionType.EXPENSE }
                    state = state.copy(selectedCategoryId = defaultCategory?.id)
                }

                _uiState.update { state }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        viewModelScope.launch {
            _uiState.update { state ->
                // 切换类型时，重新选择默认分类
                val defaultCategory = state.categories.find {
                    when (type) {
                        TransactionType.INCOME -> it.type == TransactionType.INCOME
                        else -> it.type == TransactionType.EXPENSE
                    }
                }
                state.copy(
                    transactionType = type,
                    selectedCategoryId = defaultCategory?.id
                )
            }
        }
    }

    fun setAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun setCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setAccount(accountId: String) {
        val account = _uiState.value.accounts.find { it.id == accountId }
        _uiState.update {
            it.copy(
                selectedAccountId = accountId,
                selectedAccountName = account?.name ?: "现金"
            )
        }
    }

    fun setToAccount(accountId: String) {
        _uiState.update { it.copy(toAccountId = accountId) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun setDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun saveTransaction() {
        if (!_uiState.value.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val state = _uiState.value
                val amount = state.amount.toDoubleOrNull() ?: 0.0

                val transaction = Transaction(
                    id = transactionId ?: UUID.randomUUID().toString(),
                    type = state.transactionType,
                    amount = amount,
                    categoryId = state.selectedCategoryId!!,
                    accountId = state.selectedAccountId!!,
                    toAccountId = state.toAccountId,
                    bookId = state.bookId,
                    date = state.date,
                    note = state.note,
                    tags = state.tags
                )

                if (state.isEditing && originalTransaction != null) {
                    storageManager.updateTransaction(originalTransaction!!, transaction)
                } else {
                    storageManager.addTransaction(transaction)
                }

                _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isSaving = false, error = e.message)
                }
            }
        }
    }

    fun deleteTransaction() {
        if (originalTransaction == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                storageManager.deleteTransaction(originalTransaction!!)
                _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isSaving = false, error = e.message)
                }
            }
        }
    }
}

class RecordViewModelFactory(
    private val context: Context,
    private val transactionId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(
            FileStorageManager(context.applicationContext),
            transactionId
        ) as T
    }
}
