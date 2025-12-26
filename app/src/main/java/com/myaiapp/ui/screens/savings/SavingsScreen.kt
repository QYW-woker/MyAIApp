package com.myaiapp.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    onBack: () -> Unit,
    viewModel: SavingsViewModel = viewModel(factory = SavingsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "存钱计划",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加计划")
                    }
                }
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
            // 总览卡片
            item {
                SavingsTotalCard(
                    totalSaved = uiState.totalSaved,
                    totalTarget = uiState.totalTarget,
                    plansCount = uiState.activePlansCount,
                    modifier = Modifier.padding(AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }

            // 计划标题
            item {
                Text(
                    text = "进行中的计划",
                    style = AppTypography.Title3,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
            }

            if (uiState.plans.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Add,
                        title = "暂无存钱计划",
                        subtitle = "点击右上角添加您的第一个存钱计划",
                        actionText = "添加计划",
                        onAction = { showAddDialog = true }
                    )
                }
            } else {
                items(uiState.plans) { plan ->
                    SavingsCard(
                        plan = plan,
                        onClick = { showDepositDialog = plan.id },
                        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // 添加计划对话框
    if (showAddDialog) {
        AddSavingsPlanDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, emoji, targetAmount ->
                viewModel.addPlan(name, emoji, targetAmount)
                showAddDialog = false
            }
        )
    }

    // 存入对话框
    showDepositDialog?.let { planId ->
        AddDepositDialog(
            onDismiss = { showDepositDialog = null },
            onConfirm = { amount, note ->
                viewModel.addDeposit(planId, amount, note)
                showDepositDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSavingsPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, targetAmount: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💰") }
    var amount by remember { mutableStateOf("") }

    val emojis = listOf("💰", "🏠", "🚗", "✈️", "💻", "📱", "👗", "💍", "🎓", "🎁")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建存钱计划") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("计划名称") },
                    placeholder = { Text("例如：旅行基金") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("选择图标", style = AppTypography.Caption, color = AppColors.Gray500)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.take(5).forEach { e ->
                        TextButton(
                            onClick = { emoji = e },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (e == emoji) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray100
                            )
                        ) {
                            Text(e, style = AppTypography.Title2)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.drop(5).forEach { e ->
                        TextButton(
                            onClick = { emoji = e },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (e == emoji) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray100
                            )
                        ) {
                            Text(e, style = AppTypography.Title2)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("目标金额") },
                    prefix = { Text("¥") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetAmount = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && targetAmount > 0) {
                        onConfirm(name, emoji, targetAmount)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDepositDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("存入") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("存入金额") },
                    prefix = { Text("¥") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val depositAmount = amount.toDoubleOrNull() ?: 0.0
                    if (depositAmount > 0) {
                        onConfirm(depositAmount, note)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
