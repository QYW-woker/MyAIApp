package com.myaiapp.ui.screens.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.export.ExportDateRange
import com.myaiapp.data.export.ExportFormat
import com.myaiapp.data.export.ExportManager
import com.myaiapp.data.export.ExportResult
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val selectedDateRange: ExportDateRange = ExportDateRange.THIS_MONTH,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val isLoadingPreview: Boolean = true,
    val transactionCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isExporting: Boolean = false,
    val exportedUri: Uri? = null,
    val errorMessage: String? = null,
    val exportedFiles: List<File> = emptyList()
)

class ExportViewModel(
    private val context: Context
) : ViewModel() {

    private val exportManager = ExportManager(context)

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private var currentTransactions: List<Transaction> = emptyList()

    init {
        loadPreview()
        loadExportedFiles()
    }

    fun setFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun setDateRange(range: ExportDateRange) {
        _uiState.update { it.copy(selectedDateRange = range) }
        loadPreview()
    }

    fun setCustomStartDate(date: Long) {
        _uiState.update { it.copy(customStartDate = date) }
        if (_uiState.value.selectedDateRange == ExportDateRange.CUSTOM) {
            loadPreview()
        }
    }

    fun setCustomEndDate(date: Long) {
        _uiState.update { it.copy(customEndDate = date) }
        if (_uiState.value.selectedDateRange == ExportDateRange.CUSTOM) {
            loadPreview()
        }
    }

    private fun loadPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPreview = true, errorMessage = null) }

            try {
                val state = _uiState.value
                currentTransactions = exportManager.getTransactionsByDateRange(
                    range = state.selectedDateRange,
                    customStart = state.customStartDate,
                    customEnd = state.customEndDate
                )

                val totalIncome = currentTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                val totalExpense = currentTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        isLoadingPreview = false,
                        transactionCount = currentTransactions.size,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoadingPreview = false,
                        errorMessage = "加载数据失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun export() {
        if (currentTransactions.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "没有可导出的数据") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }

            try {
                val result = when (_uiState.value.selectedFormat) {
                    ExportFormat.CSV -> exportManager.exportToCsv(currentTransactions)
                    ExportFormat.EXCEL -> exportManager.exportToExcel(currentTransactions)
                    ExportFormat.PDF -> exportManager.exportToPdf(currentTransactions)
                }

                when (result) {
                    is ExportResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportedUri = result.uri
                            )
                        }
                        loadExportedFiles()
                    }
                    is ExportResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "导出失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportedUri = null) }
    }

    private fun loadExportedFiles() {
        val files = exportManager.getExportedFiles()
        _uiState.update { it.copy(exportedFiles = files) }
    }

    fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = when {
                file.name.endsWith(".pdf") -> "application/pdf"
                file.name.endsWith(".csv") -> "text/csv"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "分享文件").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(errorMessage = "分享失败: ${e.message}") }
        }
    }

    fun deleteExportFile(file: File) {
        if (exportManager.deleteExportFile(file)) {
            loadExportedFiles()
        }
    }

    fun clearAllExports() {
        exportManager.clearAllExports()
        loadExportedFiles()
    }
}

class ExportViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExportViewModel(context.applicationContext) as T
    }
}
