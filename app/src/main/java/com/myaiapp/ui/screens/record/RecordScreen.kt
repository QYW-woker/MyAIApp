package com.myaiapp.ui.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.data.voice.VoiceParser
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatFullDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    transactionId: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RecordViewModel = viewModel(
        factory = RecordViewModelFactory(LocalContext.current, transactionId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceParser = remember { VoiceParser() }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date
    )

    // 账户选择器状态
    var showAccountPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved()
        }
    }

    // 处理语音输入结果
    fun handleVoiceResult(text: String) {
        val result = voiceParser.parse(text, uiState.categories)
        viewModel.setTransactionType(result.transactionType)
        result.amount?.let { viewModel.setAmount(it.toString()) }
        result.categoryId?.let { viewModel.setCategory(it) }
        result.note?.let { viewModel.setNote(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.Close, contentDescription = "关闭")
            }

            // 类型选择
            SegmentedControl(
                items = listOf("支出", "收入", "转账"),
                selectedIndex = when (uiState.transactionType) {
                    TransactionType.EXPENSE -> 0
                    TransactionType.INCOME -> 1
                    TransactionType.TRANSFER -> 2
                },
                onItemSelected = { index ->
                    viewModel.setTransactionType(
                        when (index) {
                            0 -> TransactionType.EXPENSE
                            1 -> TransactionType.INCOME
                            else -> TransactionType.TRANSFER
                        }
                    )
                },
                modifier = Modifier.width(200.dp)
            )

            Row {
                if (transactionId == null) {
                    VoiceInputButton(onResult = { handleVoiceResult(it) })
                }
                if (transactionId != null) {
                    IconButton(onClick = { viewModel.deleteTransaction() }) {
                        Icon(Icons.Outlined.Delete, "删除", tint = AppColors.Red)
                    }
                }
            }
        }

        // 金额显示区域 - 紧凑版
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 金额显示
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¥",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (uiState.transactionType) {
                            TransactionType.EXPENSE -> AppColors.Red
                            TransactionType.INCOME -> AppColors.Green
                            else -> AppColors.Blue
                        }
                    )
                    Text(
                        text = if (uiState.amount.isEmpty()) "0" else uiState.amount,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (uiState.transactionType) {
                            TransactionType.EXPENSE -> AppColors.Red
                            TransactionType.INCOME -> AppColors.Green
                            else -> AppColors.Blue
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 紧凑数字键盘 (3行)
                CompactNumberKeypad(
                    onNumberClick = { digit ->
                        if (uiState.amount.length < 10) {
                            val newAmount = when {
                                digit == "." && uiState.amount.contains(".") -> uiState.amount
                                digit == "." && uiState.amount.isEmpty() -> "0."
                                uiState.amount == "0" && digit != "." -> digit
                                else -> uiState.amount + digit
                            }
                            val parts = newAmount.split(".")
                            if (parts.size <= 1 || parts[1].length <= 2) {
                                viewModel.setAmount(newAmount)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (uiState.amount.isNotEmpty()) {
                            viewModel.setAmount(uiState.amount.dropLast(1))
                        }
                    }
                )
            }
        }

        // 分类选择
        Text(
            text = "选择分类",
            style = AppTypography.Caption,
            color = AppColors.Gray500,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // 分类网格 - 占据剩余空间
        CategoryGrid(
            categories = uiState.categories.filter {
                when (uiState.transactionType) {
                    TransactionType.INCOME -> it.type == TransactionType.INCOME
                    else -> it.type == TransactionType.EXPENSE
                }
            },
            selectedCategoryId = uiState.selectedCategoryId,
            onCategorySelected = { viewModel.setCategory(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        // 底部操作区
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                // 备注、日期、账户 - 一行显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 备注
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { viewModel.setNote(it) },
                        placeholder = { Text("备注", style = AppTypography.Caption) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Blue
                        ),
                        singleLine = true,
                        textStyle = AppTypography.Body
                    )

                    // 日期按钮
                    Surface(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Gray100
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                null,
                                tint = AppColors.Gray600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatShortDate(uiState.date),
                                style = AppTypography.Caption,
                                color = AppColors.Gray700
                            )
                        }
                    }

                    // 账户按钮
                    Surface(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable { showAccountPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Gray100
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.AccountBalanceWallet,
                                null,
                                tint = AppColors.Gray600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedAccountName,
                                style = AppTypography.Caption,
                                color = AppColors.Gray700,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 保存按钮
                Button(
                    onClick = { viewModel.saveTransaction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = uiState.isValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (uiState.transactionType) {
                            TransactionType.EXPENSE -> AppColors.Red
                            TransactionType.INCOME -> AppColors.Green
                            else -> AppColors.Blue
                        }
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("保存", style = AppTypography.Button)
                    }
                }
            }
        }
    }

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("确定", color = AppColors.Blue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 账户选择器
    if (showAccountPicker) {
        AccountPickerSheet(
            accounts = uiState.accounts,
            selectedAccountId = uiState.selectedAccountId,
            onAccountSelected = { accountId ->
                viewModel.setAccount(accountId)
                showAccountPicker = false
            },
            onDismiss = { showAccountPicker = false }
        )
    }
}

// 紧凑数字键盘
@Composable
private fun CompactNumberKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3", "4", "5"),
        listOf("6", "7", "8", "9", "0"),
        listOf("00", ".", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    val weight = if (key == "⌫") 1.5f else 1f
                    Surface(
                        modifier = Modifier
                            .weight(weight)
                            .height(44.dp)
                            .clickable {
                                if (key == "⌫") onDeleteClick() else onNumberClick(key)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (key == "⌫") AppColors.Gray200 else AppColors.Gray100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.Outlined.Backspace,
                                    "删除",
                                    tint = AppColors.Gray600,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.Gray800
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, parseColor(category.color), RoundedCornerShape(12.dp))
                    } else Modifier
                )
                .padding(2.dp)
        ) {
            CategoryIcon(
                icon = category.icon,
                color = parseColor(category.color),
                size = 40.dp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = category.name,
            style = AppTypography.Caption,
            color = if (isSelected) AppColors.Gray900 else AppColors.Gray500,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerSheet(
    accounts: List<com.myaiapp.data.local.model.AssetAccount>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "选择账户",
                style = AppTypography.Title3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (accounts.isEmpty()) {
                // 如果没有账户，显示默认选项
                val defaultAccounts = listOf(
                    "现金" to "cash",
                    "微信" to "wechat",
                    "支付宝" to "alipay",
                    "银行卡" to "bank",
                    "信用卡" to "credit"
                )
                defaultAccounts.forEach { (name, id) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onAccountSelected(id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedAccountId == id) AppColors.Blue.copy(alpha = 0.1f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (id) {
                                    "wechat" -> Icons.Outlined.Chat
                                    "alipay" -> Icons.Outlined.Payments
                                    "bank" -> Icons.Outlined.AccountBalance
                                    "credit" -> Icons.Outlined.CreditCard
                                    else -> Icons.Outlined.Money
                                },
                                contentDescription = null,
                                tint = if (selectedAccountId == id) AppColors.Blue else AppColors.Gray600
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = AppTypography.Body,
                                color = if (selectedAccountId == id) AppColors.Blue else AppColors.Gray900
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (selectedAccountId == id) {
                                Icon(
                                    Icons.Outlined.Check,
                                    null,
                                    tint = AppColors.Blue
                                )
                            }
                        }
                    }
                }
            } else {
                accounts.forEach { account ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onAccountSelected(account.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedAccountId == account.id) AppColors.Blue.copy(alpha = 0.1f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (selectedAccountId == account.id) AppColors.Blue else AppColors.Gray600
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = account.name,
                                style = AppTypography.Body,
                                color = if (selectedAccountId == account.id) AppColors.Blue else AppColors.Gray900
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (selectedAccountId == account.id) {
                                Icon(Icons.Outlined.Check, null, tint = AppColors.Blue)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatShortDate(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "今天"
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "昨天"
        else -> "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}
