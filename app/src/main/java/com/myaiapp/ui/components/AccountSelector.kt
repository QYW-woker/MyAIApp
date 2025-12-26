package com.myaiapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.model.AssetAccount
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount

/**
 * 账户选择底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectorSheet(
    accounts: List<AssetAccount>,
    selectedAccountId: String?,
    onAccountSelected: (AssetAccount) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择账户",
                style = AppTypography.Title3,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            LazyColumn {
                items(accounts) { account ->
                    AccountSelectorItem(
                        account = account,
                        isSelected = account.id == selectedAccountId,
                        onClick = {
                            onAccountSelected(account)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSelectorItem(
    account: AssetAccount,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) AppColors.Blue.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = account.icon,
                color = parseColor(account.color),
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.Gray900
                )
                Text(
                    text = formatAmount(account.balance),
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "已选中",
                    tint = AppColors.Blue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 转账账户选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferAccountSelector(
    accounts: List<AssetAccount>,
    fromAccountId: String?,
    toAccountId: String?,
    onFromAccountSelected: (AssetAccount) -> Unit,
    onToAccountSelected: (AssetAccount) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fromAccount = accounts.find { it.id == fromAccountId }
    val toAccount = accounts.find { it.id == toAccountId }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 转出账户
        TransferAccountCard(
            label = "从",
            account = fromAccount,
            onClick = { showFromPicker = true },
            modifier = Modifier.weight(1f)
        )

        // 箭头
        Icon(
            imageVector = Icons.Outlined.Check, // 用其他图标替代
            contentDescription = null,
            tint = AppColors.Gray400,
            modifier = Modifier.size(20.dp)
        )

        // 转入账户
        TransferAccountCard(
            label = "到",
            account = toAccount,
            onClick = { showToPicker = true },
            modifier = Modifier.weight(1f)
        )
    }

    // 转出账户选择器
    if (showFromPicker) {
        AccountSelectorSheet(
            accounts = accounts.filter { it.id != toAccountId },
            selectedAccountId = fromAccountId,
            onAccountSelected = onFromAccountSelected,
            onDismiss = { showFromPicker = false }
        )
    }

    // 转入账户选择器
    if (showToPicker) {
        AccountSelectorSheet(
            accounts = accounts.filter { it.id != fromAccountId },
            selectedAccountId = toAccountId,
            onAccountSelected = onToAccountSelected,
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun TransferAccountCard(
    label: String,
    account: AssetAccount?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Gray100
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (account != null) {
                CategoryIcon(
                    icon = account.icon,
                    color = parseColor(account.color),
                    size = 36.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.name,
                    style = AppTypography.Caption,
                    color = AppColors.Gray900
                )
            } else {
                Text(
                    text = "选择账户",
                    style = AppTypography.Body,
                    color = AppColors.Gray500
                )
            }
        }
    }
}
