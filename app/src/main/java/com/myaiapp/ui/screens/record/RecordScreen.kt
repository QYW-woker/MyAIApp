package com.myaiapp.ui.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType
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

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (transactionId == null) "记一笔" else "编辑记录",
                onBackClick = onBack,
                actions = {
                    if (transactionId != null) {
                        IconButton(onClick = { viewModel.deleteTransaction() }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "删除",
                                tint = AppColors.Red
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
            )

            // 金额输入
            AmountInputSection(
                amount = uiState.amount,
                currencySymbol = "¥",
                onAmountChange = { viewModel.setAmount(it) }
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

            // 分类选择
            Text(
                text = "选择分类",
                style = AppTypography.Subhead,
                color = AppColors.Gray500,
                modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
            )
            Spacer(modifier = Modifier.height(AppDimens.SpaceSM))

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
                    .padding(horizontal = AppDimens.SpaceLG)
            )

            // 底部输入区域
            BottomInputSection(
                note = uiState.note,
                date = uiState.date,
                selectedAccountName = uiState.selectedAccountName,
                onNoteChange = { viewModel.setNote(it) },
                onDateClick = { /* TODO: Show date picker */ },
                onAccountClick = { /* TODO: Show account picker */ },
                onSave = { viewModel.saveTransaction() },
                isSaving = uiState.isSaving,
                isValid = uiState.isValid
            )
        }
    }
}

@Composable
private fun AmountInputSection(
    amount: String,
    currencySymbol: String,
    onAmountChange: (String) -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AmountDisplay(
                amount = amount,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

            // 数字键盘
            NumberKeypad(
                onNumberClick = { digit ->
                    if (amount.length < 12) {
                        val newAmount = if (digit == "." && amount.contains(".")) {
                            amount
                        } else if (digit == "." && amount.isEmpty()) {
                            "0."
                        } else if (amount == "0" && digit != ".") {
                            digit
                        } else {
                            amount + digit
                        }
                        // 限制小数点后两位
                        val parts = newAmount.split(".")
                        if (parts.size > 1 && parts[1].length > 2) {
                            return@NumberKeypad
                        }
                        onAmountChange(newAmount)
                    }
                },
                onDeleteClick = {
                    if (amount.isNotEmpty()) {
                        onAmountChange(amount.dropLast(1))
                    }
                },
                onClearClick = {
                    onAmountChange("")
                }
            )
        }
    }
}

@Composable
private fun NumberKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f)
                            .clickable {
                                when (key) {
                                    "⌫" -> onDeleteClick()
                                    else -> onNumberClick(key)
                                }
                            },
                        shape = RoundedCornerShape(AppDimens.RadiusSM),
                        color = if (key == "⌫") AppColors.Gray100 else AppColors.Gray50
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.Outlined.Backspace,
                                    contentDescription = "删除",
                                    tint = AppColors.Gray600
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = AppTypography.Title2,
                                    color = AppColors.Gray900
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
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
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = parseColor(category.color),
                            shape = RoundedCornerShape(14.dp)
                        )
                    } else Modifier
                )
                .padding(2.dp)
        ) {
            CategoryIcon(
                icon = category.icon,
                color = parseColor(category.color),
                size = 44.dp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = AppTypography.Caption,
            color = if (isSelected) AppColors.Gray900 else AppColors.Gray500,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomInputSection(
    note: String,
    date: Long,
    selectedAccountName: String,
    onNoteChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    isValid: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.SpaceLG)
        ) {
            // 备注输入
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text("添加备注...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.InputRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceMD))

            // 日期和账户选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpaceMD)
            ) {
                // 日期
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDateClick),
                    shape = RoundedCornerShape(AppDimens.RadiusSM),
                    color = AppColors.Gray100
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = AppColors.Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatFullDate(date),
                            style = AppTypography.Body,
                            color = AppColors.Gray900
                        )
                    }
                }

                // 账户
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAccountClick),
                    shape = RoundedCornerShape(AppDimens.RadiusSM),
                    color = AppColors.Gray100
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = AppColors.Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedAccountName,
                            style = AppTypography.Body,
                            color = AppColors.Gray900,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

            // 保存按钮
            PrimaryButton(
                text = "保存",
                onClick = onSave,
                enabled = isValid,
                isLoading = isSaving
            )
        }
    }
}
