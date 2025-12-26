package com.myaiapp.ui.screens.record

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ocr.ScreenshotRecognizer
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 记账底部弹窗 - 支持截图OCR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordBottomSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RecordViewModel = viewModel(
        factory = RecordViewModelFactory(LocalContext.current, null)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isProcessingImage by remember { mutableStateOf(false) }
    var ocrResult by remember { mutableStateOf<String?>(null) }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessingImage = true
            scope.launch {
                try {
                    val recognizer = ScreenshotRecognizer(context)
                    val text = recognizer.recognizeFromUri(uri)
                    if (text != null && recognizer.isPaymentScreenshot(text)) {
                        val parsed = recognizer.parsePaymentText(text)
                        parsed?.let { payment ->
                            viewModel.setAmount(payment.amount.toString())
                            payment.payee?.let { viewModel.setNote(it) }
                        }
                        ocrResult = "识别成功"
                    } else {
                        ocrResult = "未识别到付款信息"
                    }
                    recognizer.close()
                } catch (e: Exception) {
                    ocrResult = "识别失败: ${e.message}"
                } finally {
                    isProcessingImage = false
                }
            }
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(bottom = 32.dp)
    ) {
        // 顶部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.Gray500)
            }

            // OCR按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isProcessingImage
                ) {
                    if (isProcessingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "截图识别",
                            tint = AppColors.Blue
                        )
                    }
                }
            }

            TextButton(
                onClick = { viewModel.saveTransaction() },
                enabled = uiState.isValid && !uiState.isSaving
            ) {
                Text(
                    "保存",
                    color = if (uiState.isValid) AppColors.Blue else AppColors.Gray400
                )
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 金额显示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AmountDisplay(
                amount = uiState.amount,
                currencySymbol = "¥",
                color = when (uiState.transactionType) {
                    TransactionType.INCOME -> AppColors.Green
                    TransactionType.EXPENSE -> AppColors.Gray900
                    TransactionType.TRANSFER -> AppColors.Blue
                }
            )
        }

        // OCR结果提示
        ocrResult?.let {
            Text(
                text = it,
                style = AppTypography.Caption,
                color = if (it.contains("成功")) AppColors.Green else AppColors.Orange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 数字键盘
        CompactNumberKeypad(
            onNumberClick = { digit ->
                val currentAmount = uiState.amount
                if (currentAmount.length < 12) {
                    val newAmount = when {
                        digit == "." && currentAmount.contains(".") -> currentAmount
                        digit == "." && currentAmount.isEmpty() -> "0."
                        currentAmount == "0" && digit != "." -> digit
                        else -> currentAmount + digit
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
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 分类选择
        Text(
            text = "选择分类",
            style = AppTypography.Caption,
            color = AppColors.Gray500,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 分类网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val filteredCategories = uiState.categories.filter {
                when (uiState.transactionType) {
                    TransactionType.INCOME -> it.type == TransactionType.INCOME
                    else -> it.type == TransactionType.EXPENSE
                }
            }

            items(filteredCategories) { category ->
                CompactCategoryItem(
                    category = category,
                    isSelected = category.id == uiState.selectedCategoryId,
                    onClick = { viewModel.setCategory(category.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 备注输入
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.setNote(it) },
            placeholder = { Text("添加备注...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.Gray200,
                focusedBorderColor = AppColors.Blue
            ),
            singleLine = true
        )
    }
}

@Composable
private fun CompactNumberKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable {
                                if (key == "⌫") onDeleteClick() else onNumberClick(key)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (key == "⌫") AppColors.Gray100 else AppColors.Gray50
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.Outlined.Backspace,
                                    contentDescription = "删除",
                                    tint = AppColors.Gray600,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = AppTypography.Title3,
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
private fun CompactCategoryItem(
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
                .size(40.dp)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = parseColor(category.color),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else Modifier
                )
                .padding(2.dp)
        ) {
            CategoryIcon(
                icon = category.icon,
                color = parseColor(category.color),
                size = 36.dp
            )
        }
        Text(
            text = category.name,
            style = AppTypography.Caption2,
            color = if (isSelected) AppColors.Gray900 else AppColors.Gray500,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
