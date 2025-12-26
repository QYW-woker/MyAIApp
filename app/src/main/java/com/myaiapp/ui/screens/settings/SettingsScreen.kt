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
            // 账本设置
            item {
                SettingsSection(title = "账本") {
                    SettingsItem(
                        icon = Icons.Outlined.Book,
                        title = "账本管理",
                        subtitle = "当前: ${uiState.currentBookName}",
                        onClick = { /* TODO: Navigate to book management */ }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.Category,
                        title = "分类管理",
                        subtitle = "自定义收支分类",
                        onClick = { /* TODO: Navigate to category management */ }
                    )
                }
            }

            // AI设置
            item {
                SettingsSection(title = "AI功能") {
                    SettingsItem(
                        icon = Icons.Outlined.SmartToy,
                        title = "AI设置",
                        subtitle = if (uiState.aiEnabled) "已配置" else "未配置",
                        onClick = { /* TODO: Navigate to AI settings */ }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsSwitchItem(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "智能分类",
                        subtitle = "自动识别消费分类",
                        checked = uiState.autoClassifyEnabled,
                        onCheckedChange = { viewModel.setAutoClassify(it) }
                    )
                }
            }

            // 安全设置
            item {
                SettingsSection(title = "安全") {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Lock,
                        title = "应用锁",
                        subtitle = "启动时需要验证",
                        checked = uiState.appLockEnabled,
                        onCheckedChange = { viewModel.setAppLock(it) }
                    )
                    if (uiState.appLockEnabled) {
                        Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Fingerprint,
                            title = "生物识别",
                            subtitle = "使用指纹或面容解锁",
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.setBiometric(it) }
                        )
                    }
                }
            }

            // 通知设置
            item {
                SettingsSection(title = "通知") {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Notifications,
                        title = "预算提醒",
                        subtitle = "预算超支时提醒",
                        checked = uiState.budgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.Schedule,
                        title = "记账提醒",
                        subtitle = "设置每日提醒时间",
                        onClick = { /* TODO: Show time picker */ }
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
                        onClick = { /* TODO: Show theme picker */ }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.CurrencyExchange,
                        title = "默认货币",
                        subtitle = uiState.defaultCurrency,
                        onClick = { /* TODO: Show currency picker */ }
                    )
                }
            }

            // 数据管理
            item {
                SettingsSection(title = "数据") {
                    SettingsItem(
                        icon = Icons.Outlined.Backup,
                        title = "备份数据",
                        subtitle = "导出所有数据",
                        onClick = { viewModel.backupData() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.Restore,
                        title = "恢复数据",
                        subtitle = "从备份恢复",
                        onClick = { /* TODO: Show file picker */ }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "导入账单",
                        subtitle = "支持微信/支付宝CSV",
                        onClick = { /* TODO: Show import options */ }
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
                    Divider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Gray100)
                    SettingsItem(
                        icon = Icons.Outlined.Help,
                        title = "帮助与反馈",
                        onClick = { /* TODO: Open help */ }
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

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AppColors.Green,
                    checkedThumbColor = Color.White
                )
            )
        }
    )
}
