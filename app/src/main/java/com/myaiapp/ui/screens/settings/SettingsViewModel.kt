package com.myaiapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val currentBookName: String = "默认账本",
    val aiEnabled: Boolean = false,
    val autoClassifyEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val budgetAlertEnabled: Boolean = true,
    val themeMode: String = "system",
    val defaultCurrency: String = "CNY"
)

class SettingsViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                val aiConfig = storageManager.getAIConfig()
                val books = storageManager.getAccountBooks()
                val currentBookId = storageManager.getCurrentBookId()
                val currentBook = books.find { it.id == currentBookId }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentBookName = currentBook?.name ?: "默认账本",
                        aiEnabled = aiConfig.apiKey.isNotBlank(),
                        autoClassifyEnabled = aiConfig.enableAutoClassify,
                        appLockEnabled = settings.enablePin || settings.enableBiometric,
                        biometricEnabled = settings.enableBiometric,
                        budgetAlertEnabled = settings.enableBudgetAlert,
                        themeMode = settings.darkMode,
                        defaultCurrency = settings.defaultCurrency
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setAutoClassify(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val aiConfig = storageManager.getAIConfig()
                storageManager.saveAIConfig(aiConfig.copy(enableAutoClassify = enabled))
                _uiState.update { it.copy(autoClassifyEnabled = enabled) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(enablePin = enabled))
                _uiState.update { it.copy(appLockEnabled = enabled) }
                if (!enabled) {
                    _uiState.update { it.copy(biometricEnabled = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(enableBiometric = enabled))
                _uiState.update { it.copy(biometricEnabled = enabled) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBudgetAlert(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(enableBudgetAlert = enabled))
                _uiState.update { it.copy(budgetAlertEnabled = enabled) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(darkMode = mode))
                _uiState.update { it.copy(themeMode = mode) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun backupData() {
        viewModelScope.launch {
            try {
                storageManager.createBackup()
                // TODO: Show success message
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
