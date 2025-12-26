package com.myaiapp.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.myaiapp.data.backup.BackupInfo
import com.myaiapp.data.backup.BackupManager
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 备份与恢复页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(context) }

    var localBackups by remember { mutableStateOf<List<BackupInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<BackupInfo?>(null) }
    var showRestoreDialog by remember { mutableStateOf<BackupInfo?>(null) }

    // 加载本地备份列表
    LaunchedEffect(Unit) {
        localBackups = backupManager.getLocalBackups()
    }

    // 导出备份文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                val result = backupManager.exportBackup(uri)
                message = when (result) {
                    is BackupManager.BackupResult.Success -> "备份导出成功"
                    is BackupManager.BackupResult.Error -> "导出失败：${result.message}"
                }
                showMessageDialog = true
                isLoading = false
                localBackups = backupManager.getLocalBackups()
            }
        }
    }

    // 导入备份文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                val result = backupManager.restoreFromBackup(uri)
                message = when (result) {
                    is BackupManager.RestoreResult.Success -> "数据恢复成功，请重启应用"
                    is BackupManager.RestoreResult.Error -> "恢复失败：${result.message}"
                }
                showMessageDialog = true
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "备份与恢复",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .padding(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceMD)
        ) {
            // 备份操作卡片
            item {
                AppCard {
                    Column(
                        modifier = Modifier.padding(AppDimens.CardPadding)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = AppColors.Blue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "创建备份",
                                style = AppTypography.Title3
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "将所有数据导出为备份文件，包括账本、交易记录、预算、存钱计划等",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 本地备份
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        val result = backupManager.createBackup()
                                        message = when (result) {
                                            is BackupManager.BackupResult.Success -> "备份成功：${result.file.name}"
                                            is BackupManager.BackupResult.Error -> "备份失败：${result.message}"
                                        }
                                        showMessageDialog = true
                                        isLoading = false
                                        localBackups = backupManager.getLocalBackups()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("本地备份")
                            }

                            // 导出备份
                            Button(
                                onClick = {
                                    val timestamp = System.currentTimeMillis()
                                    exportLauncher.launch("myaiapp_backup_$timestamp.zip")
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Blue
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("导出文件")
                            }
                        }
                    }
                }
            }

            // 恢复数据卡片
            item {
                AppCard {
                    Column(
                        modifier = Modifier.padding(AppDimens.CardPadding)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDownload,
                                contentDescription = null,
                                tint = AppColors.Green,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "恢复数据",
                                style = AppTypography.Title3
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "从备份文件恢复数据，会覆盖当前所有数据",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { importLauncher.launch("application/zip") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Green
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择备份文件恢复")
                        }
                    }
                }
            }

            // 本地备份列表
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本地备份",
                        style = AppTypography.Title3
                    )
                    if (localBackups.size > 5) {
                        TextButton(
                            onClick = {
                                backupManager.cleanOldBackups(5)
                                localBackups = backupManager.getLocalBackups()
                            }
                        ) {
                            Text("清理旧备份", color = AppColors.Orange)
                        }
                    }
                }
            }

            if (localBackups.isEmpty()) {
                item {
                    AppCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = AppColors.Gray300,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "暂无本地备份",
                                    style = AppTypography.Body,
                                    color = AppColors.Gray400
                                )
                            }
                        }
                    }
                }
            } else {
                items(localBackups) { backup ->
                    BackupItem(
                        backup = backup,
                        onRestore = { showRestoreDialog = backup },
                        onDelete = { showDeleteDialog = backup }
                    )
                }
            }

            // 底部说明
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "提示：建议定期导出备份文件到云盘或其他设备，防止数据丢失。",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }

        // Loading指示器
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Blue)
            }
        }
    }

    // 消息对话框
    if (showMessageDialog && message != null) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("提示") },
            text = { Text(message!!) },
            confirmButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("确定", color = AppColors.Blue)
                }
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除备份") },
            text = { Text("确定要删除备份 ${backup.formattedDate} 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        backupManager.deleteBackup(backup.file)
                        localBackups = backupManager.getLocalBackups()
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        )
    }

    // 恢复确认对话框
    showRestoreDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("恢复数据") },
            text = { Text("确定要从备份 ${backup.formattedDate} 恢复数据吗？\n\n这将覆盖当前所有数据！") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = null
                        isLoading = true
                        scope.launch {
                            val uri = Uri.fromFile(backup.file)
                            val result = backupManager.restoreFromBackup(uri)
                            message = when (result) {
                                is BackupManager.RestoreResult.Success -> "数据恢复成功，请重启应用"
                                is BackupManager.RestoreResult.Error -> "恢复失败：${result.message}"
                            }
                            showMessageDialog = true
                            isLoading = false
                        }
                    }
                ) {
                    Text("恢复", color = AppColors.Blue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        )
    }
}

@Composable
private fun BackupItem(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderZip,
                contentDescription = null,
                tint = AppColors.Blue,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.formattedDate,
                    style = AppTypography.Body
                )
                Text(
                    text = backup.formattedSize,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = "恢复",
                    tint = AppColors.Green
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Red
                )
            }
        }
    }
}
