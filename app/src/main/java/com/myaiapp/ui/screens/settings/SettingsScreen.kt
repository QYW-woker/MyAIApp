package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "设置",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = AppDimens.SpaceLG)
        ) {
            // 通知设置
            item {
                SettingsSection(title = "通知") {
                    SettingsItem(
                        icon = Icons.Outlined.Notifications,
                        title = "提醒设置",
                        subtitle = "记账提醒、预算提醒等",
                        onClick = onNavigateToReminder
                    )
                }
            }

            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    SettingsItem(
                        icon = Icons.Outlined.Palette,
                        title = "主题",
                        subtitle = when (uiState.themeMode) {
                            "system" -> "跟随系统"
                            "light" -> "浅色模式"
                            else -> "深色模式"
                        },
                        onClick = { }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.CurrencyExchange,
                        title = "货币设置",
                        subtitle = "默认: ${uiState.defaultCurrency}",
                        onClick = onNavigateToCurrency
                    )
                }
            }

            // 数据管理
            item {
                SettingsSection(title = "数据") {
                    SettingsItem(
                        icon = Icons.Outlined.Backup,
                        title = "备份与恢复",
                        subtitle = "导出或恢复数据",
                        onClick = onNavigateToBackup
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "导入账单",
                        subtitle = "支持微信/支付宝CSV",
                        onClick = onNavigateToImport
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "版本",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = AppDimens.SpaceSM)
    ) {
        Text(
            text = title,
            style = AppTypography.Caption,
            color = AppColors.Gray500,
            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.SpaceLG),
            shape = RoundedCornerShape(AppDimens.RadiusMD),
            color = Color.White
        ) {
            Column(content = content)
        }
    }
}
