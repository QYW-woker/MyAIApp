package com.myaiapp.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.myaiapp.data.import.BillImporter
import com.myaiapp.data.import.BillSource
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 账单导入页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageManager = remember { FileStorageManager(context) }
    val importer = remember { BillImporter(context, storageManager) }

    var selectedSource by remember { mutableStateOf<BillSource?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedSource != null) {
            isImporting = true
            scope.launch {
                try {
                    val bookId = storageManager.getCurrentBookId()
                    val accounts = storageManager.getAssetAccounts()
                    val accountId = when (selectedSource) {
                        BillSource.WECHAT -> accounts.find { it.name.contains("微信") }?.id
                        BillSource.ALIPAY -> accounts.find { it.name.contains("支付宝") }?.id
                        null -> null
                    } ?: accounts.firstOrNull()?.id ?: ""

                    val result = importer.importFromCsv(uri, bookId, accountId, selectedSource!!)
                    importResult = when (result) {
                        is BillImporter.ImportResult.Success ->
                            "导入成功！\n共导入 ${result.count} 条记录\n跳过 ${result.skipped} 条"
                        is BillImporter.ImportResult.Error ->
                            "导入失败：${result.message}"
                    }
                } catch (e: Exception) {
                    importResult = "导入失败：${e.message}"
                } finally {
                    isImporting = false
                    showResultDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "导入账单",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .padding(AppDimens.SpaceLG)
        ) {
            // 说明卡片
            AppCard {
                Column(
                    modifier = Modifier.padding(AppDimens.CardPadding)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppColors.Blue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "如何导出账单",
                            style = AppTypography.Title3
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "微信：我 → 服务 → 钱包 → 账单 → 常见问题 → 下载账单",
                        style = AppTypography.Body,
                        color = AppColors.Gray600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支付宝：我的 → 账单 → ... → 开具交易流水证明",
                        style = AppTypography.Body,
                        color = AppColors.Gray600
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpaceXL))

            Text(
                text = "选择导入来源",
                style = AppTypography.Title3
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))

            // 微信导入
            ImportSourceCard(
                icon = "message_circle",
                title = "微信账单",
                subtitle = "支持微信支付账单CSV文件",
                color = "#07C160",
                isLoading = isImporting && selectedSource == BillSource.WECHAT,
                onClick = {
                    selectedSource = BillSource.WECHAT
                    filePickerLauncher.launch("text/*")
                }
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))

            // 支付宝导入
            ImportSourceCard(
                icon = "smartphone",
                title = "支付宝账单",
                subtitle = "支持支付宝账单CSV文件",
                color = "#1677FF",
                isLoading = isImporting && selectedSource == BillSource.ALIPAY,
                onClick = {
                    selectedSource = BillSource.ALIPAY
                    filePickerLauncher.launch("text/*")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 注意事项
            Text(
                text = "注意：导入的账单会自动匹配分类，部分无法识别的交易将使用默认分类。",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
        }
    }

    // 结果对话框
    if (showResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("导入结果") },
            text = { Text(importResult!!) },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun ImportSourceCard(
    icon: String,
    title: String,
    subtitle: String,
    color: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    AppCard(
        onClick = if (isLoading) null else onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = icon,
                color = parseColor(color),
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.Title3
                )
                Text(
                    text = subtitle,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.Gray400
                )
            }
        }
    }
}
