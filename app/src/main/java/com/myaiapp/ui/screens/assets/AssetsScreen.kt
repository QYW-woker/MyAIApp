package com.myaiapp.ui.screens.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.AssetAccount
import com.myaiapp.data.local.model.AssetType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onBack: () -> Unit,
    viewModel: AssetsViewModel = viewModel(factory = AssetsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AssetAccount?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "我的资产",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountSheet = true },
                containerColor = AppColors.Blue
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "添加账户",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 资产总览卡片
            item {
                AssetsSummaryCard(
                    totalAssets = uiState.totalAssets,
                    totalLiabilities = uiState.totalLiabilities,
                    netWorth = uiState.netWorth
                )
            }

            // 资产账户
            if (uiState.assetAccounts.isNotEmpty()) {
                item {
                    Text(
                        text = "资产账户",
                        style = AppTypography.Subhead,
                        color = AppColors.Gray500
                    )
                }

                items(
                    items = uiState.assetAccounts,
                    key = { it.id }
                ) { account ->
                    AccountCard(
                        account = account,
                        onClick = { editingAccount = account }
                    )
                }
            }

            // 负债账户
            if (uiState.liabilityAccounts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "负债账户",
                        style = AppTypography.Subhead,
                        color = AppColors.Gray500
                    )
                }

                items(
                    items = uiState.liabilityAccounts,
                    key = { it.id }
                ) { account ->
                    AccountCard(
                        account = account,
                        onClick = { editingAccount = account }
                    )
                }
            }

            // 空状态
            if (uiState.assetAccounts.isEmpty() && uiState.liabilityAccounts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "暂无账户",
                        subtitle = "点击右下角按钮添加账户",
                        actionText = "添加账户",
                        onAction = { showAddAccountSheet = true }
                    )
                }
            }
        }
    }

    // 添加账户弹窗
    if (showAddAccountSheet) {
        AddAccountSheet(
            onDismiss = { showAddAccountSheet = false },
            onAdd = { name, type, balance, icon, color ->
                viewModel.addAccount(name, type, balance, icon, color)
                showAddAccountSheet = false
            }
        )
    }

    // 编辑账户弹窗
    editingAccount?.let { account ->
        EditAccountSheet(
            account = account,
            onDismiss = { editingAccount = null },
            onSave = { name, balance ->
                viewModel.updateAccount(account.copy(name = name, balance = balance))
                editingAccount = null
            },
            onDelete = {
                viewModel.deleteAccount(account.id)
                editingAccount = null
            }
        )
    }
}

@Composable
private fun AssetsSummaryCard(
    totalAssets: Double,
    totalLiabilities: Double,
    netWorth: Double
) {
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "净资产",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(netWorth),
                style = AppTypography.AmountLarge,
                color = if (netWorth >= 0) AppColors.Gray900 else AppColors.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = AppColors.Gray100)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总资产",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatAmount(totalAssets),
                        style = AppTypography.AmountMedium,
                        color = AppColors.Green
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "总负债",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatAmount(totalLiabilities),
                        style = AppTypography.AmountMedium,
                        color = AppColors.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: AssetAccount,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账户图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = parseColor(account.color).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAccountIcon(account.type),
                    contentDescription = null,
                    tint = parseColor(account.color),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = AppTypography.Body,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = getAccountTypeName(account.type),
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            Text(
                text = formatAmount(account.balance),
                style = AppTypography.AmountSmall,
                color = if (account.balance >= 0) AppColors.Gray900 else AppColors.Red
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountSheet(
    onDismiss: () -> Unit,
    onAdd: (String, AssetType, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.CASH) }
    var balance by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "添加账户",
                style = AppTypography.Title2,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 账户名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("账户名称") },
                placeholder = { Text("如：招商银行储蓄卡") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 账户类型
            Text(
                text = "账户类型",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AssetType.CASH to "现金",
                    AssetType.DEBIT_CARD to "储蓄卡",
                    AssetType.CREDIT_CARD to "信用卡",
                    AssetType.ALIPAY to "支付宝",
                    AssetType.WECHAT to "微信"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label, style = AppTypography.Caption) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 初始余额
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("初始余额") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Text("¥", style = AppTypography.Body) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 确认按钮
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val balanceValue = balance.toDoubleOrNull() ?: 0.0
                        val icon = when (selectedType) {
                            AssetType.CASH -> "wallet"
                            AssetType.CREDIT_CARD -> "credit_card"
                            AssetType.ALIPAY -> "smartphone"
                            AssetType.WECHAT -> "message_circle"
                            else -> "landmark"
                        }
                        val color = when (selectedType) {
                            AssetType.CASH -> "#4CD964"
                            AssetType.CREDIT_CARD -> "#FF6B6B"
                            AssetType.ALIPAY -> "#5B8DEF"
                            AssetType.WECHAT -> "#4CD964"
                            else -> "#5B8DEF"
                        }
                        onAdd(name, selectedType, balanceValue, icon, color)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue)
            ) {
                Text("添加账户", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountSheet(
    account: AssetAccount,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var balance by remember { mutableStateOf(account.balance.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "编辑账户",
                    style = AppTypography.Title2,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = AppColors.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 账户名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("账户名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 当前余额
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                label = { Text("当前余额") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Text("¥", style = AppTypography.Body) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val balanceValue = balance.toDoubleOrNull() ?: 0.0
                        onSave(name, balanceValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue)
            ) {
                Text("保存修改", fontWeight = FontWeight.Medium)
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除账户") },
            text = { Text("确定要删除账户 \"${account.name}\" 吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("删除", color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        )
    }
}

private fun getAccountIcon(type: AssetType): ImageVector {
    return when (type) {
        AssetType.CASH -> Icons.Outlined.Money
        AssetType.DEBIT_CARD -> Icons.Outlined.AccountBalance
        AssetType.CREDIT_CARD -> Icons.Outlined.CreditCard
        AssetType.ALIPAY -> Icons.Outlined.Payments
        AssetType.WECHAT -> Icons.Outlined.Chat
        AssetType.INVESTMENT -> Icons.Outlined.TrendingUp
        AssetType.RECEIVABLE -> Icons.Outlined.CallReceived
        AssetType.PAYABLE -> Icons.Outlined.CallMade
    }
}

private fun getAccountTypeName(type: AssetType): String {
    return when (type) {
        AssetType.CASH -> "现金"
        AssetType.DEBIT_CARD -> "储蓄卡"
        AssetType.CREDIT_CARD -> "信用卡"
        AssetType.ALIPAY -> "支付宝"
        AssetType.WECHAT -> "微信"
        AssetType.INVESTMENT -> "投资账户"
        AssetType.RECEIVABLE -> "应收款"
        AssetType.PAYABLE -> "应付款"
    }
}
