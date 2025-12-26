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
    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf<AssetAccount?>(null) }
    var showTransferSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "我的资产",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showTransferSheet = true }) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = "转账")
                    }
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加账户")
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
            // 资产总览
            item {
                AssetOverviewCard(
                    totalAssets = uiState.totalAssets,
                    totalLiabilities = uiState.totalLiabilities,
                    netWorth = uiState.netWorth,
                    modifier = Modifier.padding(AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }

            // 资产账户分组
            val groupedAccounts = uiState.accounts.groupBy { getAccountTypeGroup(it.type) }

            groupedAccounts.forEach { (group, accounts) ->
                item {
                    AccountGroupHeader(
                        title = group,
                        total = accounts.sumOf { it.balance }
                    )
                }

                items(accounts) { account ->
                    AccountItem(
                        account = account,
                        onClick = { showEditSheet = account }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                }
            }
        }
    }

    // 添加账户弹窗
    if (showAddSheet) {
        AccountEditSheet(
            onDismiss = { showAddSheet = false },
            onSave = { account ->
                viewModel.saveAccount(account)
                showAddSheet = false
            }
        )
    }

    // 编辑账户弹窗
    showEditSheet?.let { account ->
        AccountEditSheet(
            account = account,
            onDismiss = { showEditSheet = null },
            onSave = { updatedAccount ->
                viewModel.saveAccount(updatedAccount)
                showEditSheet = null
            },
            onDelete = { deletedAccount ->
                viewModel.deleteAccount(deletedAccount)
                showEditSheet = null
            }
        )
    }

    // 转账弹窗
    if (showTransferSheet) {
        TransferSheet(
            accounts = uiState.accounts,
            onDismiss = { showTransferSheet = false },
            onTransfer = { fromId, toId, amount, note ->
                viewModel.transfer(fromId, toId, amount, note)
                showTransferSheet = false
            }
        )
    }
}

private fun getAccountTypeGroup(type: AssetType): String {
    return when (type) {
        AssetType.CASH -> "现金"
        AssetType.DEBIT_CARD -> "储蓄账户"
        AssetType.CREDIT_CARD -> "信用账户"
        AssetType.ALIPAY, AssetType.WECHAT -> "电子钱包"
        AssetType.INVESTMENT -> "投资账户"
        AssetType.RECEIVABLE -> "应收款项"
        AssetType.PAYABLE -> "应付款项"
    }
}

@Composable
private fun AccountGroupHeader(
    title: String,
    total: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.Subhead,
            color = AppColors.Gray500
        )
        Text(
            text = formatAmount(total),
            style = AppTypography.Subhead,
            color = if (total >= 0) AppColors.Gray900 else AppColors.Red
        )
    }
}

@Composable
private fun AccountItem(
    account: AssetAccount,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppDimens.RadiusMD),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.SpaceLG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账户图标
            CategoryIcon(
                icon = account.icon,
                color = parseColor(account.color)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // 账户信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.Gray900
                )
                if (account.type == AssetType.CREDIT_CARD && account.creditLimit != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "额度 ${formatAmount(account.creditLimit!!)}",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }

            // 余额
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmount(account.balance),
                    style = AppTypography.AmountSmall,
                    color = when {
                        account.type == AssetType.CREDIT_CARD -> AppColors.Red
                        account.balance >= 0 -> AppColors.Gray900
                        else -> AppColors.Red
                    }
                )
                if (account.type == AssetType.CREDIT_CARD && account.creditLimit != null) {
                    val available = account.creditLimit!! + account.balance
                    Text(
                        text = "可用 ${formatAmount(available)}",
                        style = AppTypography.Caption2,
                        color = AppColors.Gray400
                    )
                }
            }
        }
    }
}
