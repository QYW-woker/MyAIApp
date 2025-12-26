package com.myaiapp.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.sync.BackupInfo
import com.myaiapp.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CloudSyncUiState(
    val isLoading: Boolean = true,
    val lastSyncTime: Long = 0,
    val syncEnabled: Boolean = false,
    val autoSync: Boolean = false,
    val syncOnWifiOnly: Boolean = true,
    val backups: List<BackupInfo> = emptyList(),
    val message: String? = null
)

class CloudSyncViewModel(
    private val context: Context
) : ViewModel() {

    private val syncManager = CloudSyncManager(context)

    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val status = syncManager.getSyncStatus()
                val backups = syncManager.getBackupList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastSyncTime = status.lastSyncTime,
                        syncEnabled = status.syncEnabled,
                        autoSync = status.autoSync,
                        syncOnWifiOnly = status.syncOnWifiOnly,
                        backups = backups
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = syncManager.createBackup()
                result.fold(
                    onSuccess = { fileName ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                lastSyncTime = System.currentTimeMillis(),
                                backups = syncManager.getBackupList(),
                                message = "备份成功"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "备份失败: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "备份失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun restoreBackup(fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = syncManager.restoreFromBackup(fileName)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "数据恢复成功"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "恢复失败: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "恢复失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteBackup(fileName: String) {
        viewModelScope.launch {
            try {
                val success = syncManager.deleteBackup(fileName)
                if (success) {
                    _uiState.update {
                        it.copy(
                            backups = syncManager.getBackupList(),
                            message = "备份已删除"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(message = "删除失败")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(message = "删除失败: ${e.message}")
                }
            }
        }
    }

    fun exportBackup(fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = syncManager.exportBackup(fileName)
                result.fold(
                    onSuccess = { file ->
                        // 分享文件
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        context.startActivity(Intent.createChooser(shareIntent, "分享备份文件").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })

                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "导出失败: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "导出失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateSyncEnabled(enabled: Boolean) {
        syncManager.updateSyncSettings(syncEnabled = enabled)
        _uiState.update { it.copy(syncEnabled = enabled) }
    }

    fun updateAutoSync(enabled: Boolean) {
        syncManager.updateSyncSettings(autoSync = enabled)
        _uiState.update { it.copy(autoSync = enabled) }
    }

    fun updateSyncOnWifiOnly(enabled: Boolean) {
        syncManager.updateSyncSettings(syncOnWifiOnly = enabled)
        _uiState.update { it.copy(syncOnWifiOnly = enabled) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

class CloudSyncViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CloudSyncViewModel(context.applicationContext) as T
    }
}
