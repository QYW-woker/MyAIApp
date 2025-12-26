package com.myaiapp.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.model.Budget
import com.myaiapp.data.local.model.BudgetPeriod
import com.myaiapp.data.local.model.BudgetType
import com.myaiapp.data.local.model.Category
import com.myaiapp.ui.components.CategoryIcon
import com.myaiapp.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
    budget: Budget? = null,
    onDelete: ((Budget) -> Unit)? = null,
    bookId: String = "default"
) {
    val isEdit = budget != null
    var name by remember { mutableStateOf(budget?.name ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId ?: "") }

    // 过滤只显示支出类型的分类
    val expenseCategories = categories.filter { it.type.name == "EXPENSE" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.SpaceLG)
                .padding(bottom = 32.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "编辑预算" else "添加预算",
                    style = AppTypography.Title2
                )
                Row {
                    if (isEdit && onDelete != null && budget != null) {
                        IconButton(onClick = { onDelete(budget) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "删除",
                                tint = AppColors.Red
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 预算名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("预算名称") },
                placeholder = { Text("例如：餐饮预算") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Blue,
                    unfocusedBorderColor = AppColors.Gray200
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 预算金额
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = newValue
                    }
                },
                label = { Text("预算金额") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("¥", style = AppTypography.Body) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Blue,
                    unfocusedBorderColor = AppColors.Gray200
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 选择分类
            Text(
                text = "选择分类",
                style = AppTypography.Subhead,
                color = AppColors.Gray600
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenseCategories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategoryId = category.id }
                            .background(
                                if (isSelected) parseColor(category.color).copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) parseColor(category.color)
                                    else parseColor(category.color).copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CategoryIcon(
                                icon = category.icon,
                                color = if (isSelected) Color.White else parseColor(category.color),
                                size = 24.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name,
                            style = AppTypography.Caption,
                            color = if (isSelected) parseColor(category.color) else AppColors.Gray600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amountValue > 0) {
                        val newBudget = Budget(
                            id = budget?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = if (selectedCategoryId.isNotEmpty()) BudgetType.CATEGORY else BudgetType.TOTAL,
                            categoryId = selectedCategoryId.ifEmpty { null },
                            amount = amountValue,
                            period = BudgetPeriod.MONTHLY,
                            startDate = budget?.startDate ?: System.currentTimeMillis(),
                            bookId = budget?.bookId ?: bookId
                        )
                        onSave(newBudget)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Blue
                ),
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = if (isEdit) "保存修改" else "添加预算",
                    style = AppTypography.Body
                )
            }
        }
    }
}
