package com.myaiapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencySettingsUiState(
    val isLoading: Boolean = true,
    val defaultCurrency: String = "CNY",
    val currencies: List<Currency> = emptyList(),
    val lastUpdated: Long = 0
)

class CurrencySettingsViewModel(
    private val context: Context
) : ViewModel() {

    private val storageManager = FileStorageManager(context)

    private val _uiState = MutableStateFlow(CurrencySettingsUiState())
    val uiState: StateFlow<CurrencySettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val settings = storageManager.getSettings()
                val currencies = storageManager.getCurrencies()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        defaultCurrency = settings.defaultCurrency,
                        currencies = currencies.sortedWith(
                            compareBy(
                                { it.code != settings.defaultCurrency },
                                { it.code }
                            )
                        ),
                        lastUpdated = currencies.firstOrNull()?.lastUpdated ?: 0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setDefaultCurrency(code: String) {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(defaultCurrency = code))

                _uiState.update {
                    it.copy(
                        defaultCurrency = code,
                        currencies = it.currencies.sortedWith(
                            compareBy(
                                { curr -> curr.code != code },
                                { curr -> curr.code }
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 这里可以调用汇率API获取最新汇率
                // 暂时使用预设汇率
                val updatedCurrencies = listOf(
                    Currency("CNY", "人民币", "¥", 1.0, System.currentTimeMillis()),
                    Currency("USD", "美元", "$", 7.24, System.currentTimeMillis()),
                    Currency("EUR", "欧元", "€", 7.85, System.currentTimeMillis()),
                    Currency("GBP", "英镑", "£", 9.15, System.currentTimeMillis()),
                    Currency("JPY", "日元", "¥", 0.048, System.currentTimeMillis()),
                    Currency("HKD", "港币", "HK$", 0.93, System.currentTimeMillis()),
                    Currency("TWD", "新台币", "NT$", 0.22, System.currentTimeMillis()),
                    Currency("KRW", "韩元", "₩", 0.0054, System.currentTimeMillis()),
                    Currency("SGD", "新加坡元", "S$", 5.38, System.currentTimeMillis()),
                    Currency("AUD", "澳元", "A$", 4.72, System.currentTimeMillis()),
                    Currency("CAD", "加元", "C$", 5.32, System.currentTimeMillis()),
                    Currency("CHF", "瑞士法郎", "Fr", 8.15, System.currentTimeMillis()),
                    Currency("THB", "泰铢", "฿", 0.20, System.currentTimeMillis()),
                    Currency("MYR", "马来西亚林吉特", "RM", 1.53, System.currentTimeMillis()),
                    Currency("VND", "越南盾", "₫", 0.00029, System.currentTimeMillis())
                )

                storageManager.saveCurrencies(updatedCurrencies)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currencies = updatedCurrencies.sortedWith(
                            compareBy(
                                { curr -> curr.code != it.defaultCurrency },
                                { curr -> curr.code }
                            )
                        ),
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addCustomCurrency(code: String, name: String, symbol: String, rate: Double) {
        viewModelScope.launch {
            try {
                val newCurrency = Currency(code, name, symbol, rate, System.currentTimeMillis())
                val currencies = storageManager.getCurrencies().toMutableList()
                currencies.add(newCurrency)
                storageManager.saveCurrencies(currencies)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class CurrencySettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencySettingsViewModel(context.applicationContext) as T
    }
}
