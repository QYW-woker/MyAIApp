package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.sync.BackupInfo
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onBack: () -> Unit,
    viewModel: CloudSyncViewModel = viewModel(factory = CloudSyncViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "数据备份与同步",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 同步状态卡片
            item {
                SyncStatusCard(
                    lastSyncTime = uiState.lastSyncTime,
                    isLoading = uiState.isLoading,
                    onBackupNow = { viewModel.createBackup() }
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
            }

            // 同步设置
            item {
                Text(
                    text = "同步设置",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
                )
            }

            item {
                AppCard(
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                ) {
                    Column {
                        SyncSettingItem(
                            icon = Icons.Outlined.CloudSync,
                            title = "启用云同步",
                            subtitle = "自动同步数据到云端（开发中）",
                            checked = uiState.syncEnabled,
                            onCheckedChange = { viewModel.updateSyncEnabled(it) },
                            enabled = false // 暂未实现真正的云同步
                        )

                        HorizontalDivider(color = AppColors.Gray100)

                        SyncSettingItem(
                            icon = Icons.Outlined.Autorenew,
                            title = "自动备份",
                            subtitle = "每天自动创建本地备份",
                            checked = uiState.autoSync,
                            onCheckedChange = { viewModel.updateAutoSync(it) }
                        )

                        HorizontalDivider(color = AppColors.Gray100)

                        SyncSettingItem(
                            icon = Icons.Outlined.Wifi,
                            title = "仅WiFi同步",
                            subtitle = "仅在WiFi环境下进行同步",
                            checked = uiState.syncOnWifiOnly,
                            onCheckedChange = { viewModel.updateSyncOnWifiOnly(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
            }

            // 备份列表标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本地备份",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        text = "${uiState.backups.size}个备份",
                        style = AppTypography.Caption,
                        color = AppColors.Gray400
                    )
                }
            }

            // 备份列表
            if (uiState.backups.isEmpty()) {
                item {
                    AppCard(
                        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDimens.SpaceXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.Gray300
                            )
                            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))
                            Text(
                                text = "暂无备份",
                                style = AppTypography.Body,
                                color = AppColors.Gray500
                            )
                            Text(
                                text = "点击上方按钮创建首个备份",
                                style = AppTypography.Caption,
                                color = AppColors.Gray400
                            )
                        }
                    }
                }
            } else {
                items(uiState.backups) { backup ->
                    BackupItem(
                        backup = backup,
                        onRestore = { showRestoreConfirm = backup.fileName },
                        onExport = { viewModel.exportBackup(backup.fileName) },
                        onDelete = { showDeleteConfirm = backup.fileName }
                    )
                }
            }

            // 底部说明
            item {
                Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                Text(
                    text = "备份数据包含账本、交易记录、分类、预算、存钱计划等所有数据。\n系统会自动保留最近5个备份。",
                    style = AppTypography.Caption,
                    color = AppColors.Gray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.SpaceLG)
                )
            }
        }

        // 加载指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        }
    }

    // 删除确认对话框
    showDeleteConfirm?.let { fileName ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除备份") },
            text = { Text("确定要删除此备份吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup(fileName)
                        showDeleteConfirm = null
                    }
                ) {
                    Text("删除", color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 恢复确认对话框
    showRestoreConfirm?.let { fileName ->
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = null },
            title = { Text("恢复数据") },
            text = { Text("确定要从此备份恢复数据吗？现有数据可能会被覆盖。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreBackup(fileName)
                        showRestoreConfirm = null
                    }
                ) {
                    Text("恢复", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 显示消息
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }

        Snackbar(
            modifier = Modifier
                .padding(AppDimens.SpaceLG)
        ) {
            Text(message)
        }
    }
}

@Composable
private fun SyncStatusCard(
    lastSyncTime: Long,
    isLoading: Boolean,
    onBackupNow: () -> Unit
) {
    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = AppColors.Primary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))

            Text(
                text = if (lastSyncTime > 0) "已备份" else "未备份",
                style = AppTypography.Title2,
                fontWeight = FontWeight.Bold
            )

            if (lastSyncTime > 0) {
                Text(
                    text = "上次备份: ${formatDateTime(lastSyncTime)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

            Button(
                onClick = onBackupNow,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "备份中..." else "立即备份")
            }
        }
    }
}

@Composable
private fun SyncSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.SpaceLG),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) AppColors.Gray600 else AppColors.Gray300,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(AppDimens.SpaceMD))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.Body,
                color = if (enabled) AppColors.Gray900 else AppColors.Gray400
            )
            Text(
                text = subtitle,
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppColors.Gray300
            )
        )
    }
}

@Composable
private fun BackupItem(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.SpaceLG)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Backup,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(AppDimens.SpaceMD))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDateTime(backup.timestamp),
                        style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${backup.bookCount}个账本 · ${backup.transactionCount}笔记录 · ${formatFileSize(backup.size)}",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除", color = AppColors.Gray500)
                }

                TextButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出", color = AppColors.Primary)
                }

                TextButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AppColors.Green
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("恢复", color = AppColors.Green)
                }
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "${size}B"
        size < 1024 * 1024 -> "${size / 1024}KB"
        else -> String.format("%.1fMB", size / (1024.0 * 1024.0))
    }
}
