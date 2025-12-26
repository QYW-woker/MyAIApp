package com.myaiapp.ui.screens.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.model.AssetAccount
import com.myaiapp.data.local.model.AssetType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.util.*

/**
 * 账户编辑底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditSheet(
    account: AssetAccount? = null,
    onDismiss: () -> Unit,
    onSave: (AssetAccount) -> Unit,
    onDelete: ((AssetAccount) -> Unit)? = null
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var balance by remember { mutableStateOf(account?.balance?.toString() ?: "0") }
    var creditLimit by remember { mutableStateOf(account?.creditLimit?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(account?.type ?: AssetType.CASH) }
    var selectedIcon by remember { mutableStateOf(account?.icon ?: "wallet") }
    var selectedColor by remember { mutableStateOf(account?.color ?: "#3B82F6") }
    var note by remember { mutableStateOf(account?.note ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = account != null

    val assetTypes = listOf(
        AssetType.CASH to "现金",
        AssetType.DEBIT_CARD to "储蓄卡",
        AssetType.CREDIT_CARD to "信用卡",
        AssetType.ALIPAY to "支付宝",
        AssetType.WECHAT to "微信",
        AssetType.INVESTMENT to "投资账户",
        AssetType.RECEIVABLE to "应收款",
        AssetType.PAYABLE to "应付款"
    )

    val icons = listOf(
        "wallet", "credit_card", "account_balance", "savings",
        "payments", "currency_yuan", "attach_money", "account_balance_wallet"
    )

    val colors = listOf(
        "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6",
        "#EC4899", "#06B6D4", "#84CC16", "#F97316", "#6366F1"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "编辑账户" else "添加账户",
                    style = AppTypography.Title2
                )
                if (isEditing && onDelete != null) {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("删除", color = AppColors.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 账户名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("账户名称") },
                placeholder = { Text("如：工商银行储蓄卡") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 账户类型
            Text(
                text = "账户类型",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(assetTypes) { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Blue.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Blue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 余额/欠款
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it },
                label = {
                    Text(
                        when (selectedType) {
                            AssetType.CREDIT_CARD -> "当前欠款"
                            AssetType.PAYABLE -> "应付金额"
                            AssetType.RECEIVABLE -> "应收金额"
                            else -> "当前余额"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = { Text("¥", color = AppColors.Gray500) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            // 信用卡额度
            if (selectedType == AssetType.CREDIT_CARD) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("信用额度") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = { Text("¥", color = AppColors.Gray500) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = AppColors.Gray200,
                        focusedBorderColor = AppColors.Blue
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 选择图标
            Text(
                text = "选择图标",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(icons) { icon ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (icon == selectedIcon) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = parseColor(selectedColor),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { selectedIcon = icon }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIcon(
                            icon = icon,
                            color = parseColor(selectedColor),
                            size = 40.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 选择颜色
            Text(
                text = "选择颜色",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (color == selectedColor) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = AppColors.Gray900,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            )
                            .padding(4.dp)
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = parseColor(color)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    val balanceValue = balance.toDoubleOrNull() ?: 0.0
                    val limitValue = creditLimit.toDoubleOrNull()

                    // 信用卡余额为负数（表示欠款）
                    val finalBalance = if (selectedType == AssetType.CREDIT_CARD ||
                        selectedType == AssetType.PAYABLE
                    ) {
                        -kotlin.math.abs(balanceValue)
                    } else {
                        balanceValue
                    }

                    val newAccount = AssetAccount(
                        id = account?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        type = selectedType,
                        balance = finalBalance,
                        icon = selectedIcon,
                        color = selectedColor,
                        creditLimit = if (selectedType == AssetType.CREDIT_CARD) limitValue else null,
                        note = note.takeIf { it.isNotBlank() },
                        createdAt = account?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(newAccount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Blue
                )
            ) {
                Text(
                    text = if (isEditing) "保存修改" else "添加账户",
                    style = AppTypography.Body
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && account != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除账户") },
            text = { Text("确定要删除账户「${account.name}」吗？\n\n关联的交易记录不会被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke(account)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        )
    }
}

/**
 * 账户转账弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    accounts: List<AssetAccount>,
    onDismiss: () -> Unit,
    onTransfer: (fromId: String, toId: String, amount: Double, note: String) -> Unit
) {
    var fromAccount by remember { mutableStateOf<AssetAccount?>(null) }
    var toAccount by remember { mutableStateOf<AssetAccount?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "账户转账",
                style = AppTypography.Title2
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 转出账户
            Text(
                text = "转出账户",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFromPicker = true },
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Gray50,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fromAccount != null) {
                        CategoryIcon(
                            icon = fromAccount!!.icon,
                            color = parseColor(fromAccount!!.color),
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = fromAccount!!.name,
                            style = AppTypography.Body,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "选择转出账户",
                            style = AppTypography.Body,
                            color = AppColors.Gray400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = AppColors.Gray400
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 转入账户
            Text(
                text = "转入账户",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showToPicker = true },
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Gray50,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (toAccount != null) {
                        CategoryIcon(
                            icon = toAccount!!.icon,
                            color = parseColor(toAccount!!.color),
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = toAccount!!.name,
                            style = AppTypography.Body,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "选择转入账户",
                            style = AppTypography.Body,
                            color = AppColors.Gray400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = AppColors.Gray400
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 转账金额
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("转账金额") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = { Text("¥", color = AppColors.Gray500) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 确认转账
            Button(
                onClick = {
                    val transferAmount = amount.toDoubleOrNull() ?: 0.0
                    if (fromAccount != null && toAccount != null && transferAmount > 0) {
                        onTransfer(fromAccount!!.id, toAccount!!.id, transferAmount, note)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = fromAccount != null && toAccount != null &&
                        fromAccount != toAccount &&
                        (amount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Blue
                )
            ) {
                Text("确认转账", style = AppTypography.Body)
            }
        }
    }

    // 账户选择器
    if (showFromPicker) {
        AccountPickerDialog(
            accounts = accounts.filter { it.id != toAccount?.id },
            onSelect = {
                fromAccount = it
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        AccountPickerDialog(
            accounts = accounts.filter { it.id != fromAccount?.id },
            onSelect = {
                toAccount = it
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun AccountPickerDialog(
    accounts: List<AssetAccount>,
    onSelect: (AssetAccount) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择账户") },
        text = {
            Column {
                accounts.forEach { account ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(account) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIcon(
                                icon = account.icon,
                                color = parseColor(account.color),
                                size = 36.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = AppTypography.Body
                                )
                                Text(
                                    text = "余额: ¥${String.format("%.2f", account.balance)}",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray500
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.Gray500)
            }
        }
    )
}
