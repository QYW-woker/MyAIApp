package com.myaiapp.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.myaiapp.data.local.model.Budget
import com.myaiapp.data.local.model.BudgetPeriod
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.util.*

/**
 * 预算编辑底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditSheet(
    budget: Budget? = null,
    categories: List<BudgetCategory>,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
    onDelete: ((Budget) -> Unit)? = null
) {
    var name by remember { mutableStateOf(budget?.name ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }
    var selectedPeriod by remember { mutableStateOf(budget?.period ?: BudgetPeriod.MONTHLY) }
    var alertThreshold by remember { mutableStateOf((budget?.alertThreshold ?: 0.8).toString()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = budget != null

    val periods = listOf(
        BudgetPeriod.WEEKLY to "每周",
        BudgetPeriod.MONTHLY to "每月",
        BudgetPeriod.YEARLY to "每年"
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
                    text = if (isEditing) "编辑预算" else "添加预算",
                    style = AppTypography.Title2
                )
                if (isEditing && onDelete != null) {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("删除", color = AppColors.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 预算名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("预算名称") },
                placeholder = { Text("如：日常开销、餐饮预算") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 预算金额
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("预算金额") },
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

            // 预算周期
            Text(
                text = "预算周期",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periods.forEach { (period, label) ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Blue.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Blue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 关联分类（可选）
            Text(
                text = "关联分类（可选）",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 不关联分类选项
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("全部分类") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AllInclusive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Blue.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Blue
                        )
                    )
                }

                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        parseColor(category.color),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = parseColor(category.color).copy(alpha = 0.1f),
                            selectedLabelColor = parseColor(category.color)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 预警阈值
            Text(
                text = "预警阈值",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("0.5" to "50%", "0.7" to "70%", "0.8" to "80%", "0.9" to "90%").forEach { (value, label) ->
                    FilterChip(
                        selected = alertThreshold == value,
                        onClick = { alertThreshold = value },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Orange.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Orange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当支出达到预算的${((alertThreshold.toDoubleOrNull() ?: 0.8) * 100).toInt()}%时发出提醒",
                style = AppTypography.Caption,
                color = AppColors.Gray400
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    val budgetAmount = amount.toDoubleOrNull() ?: 0.0
                    val threshold = alertThreshold.toDoubleOrNull() ?: 0.8

                    val newBudget = Budget(
                        id = budget?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        amount = budgetAmount,
                        period = selectedPeriod,
                        categoryId = selectedCategoryId,
                        alertThreshold = threshold,
                        isActive = budget?.isActive ?: true,
                        createdAt = budget?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(newBudget)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Blue
                )
            ) {
                Text(
                    text = if (isEditing) "保存修改" else "添加预算",
                    style = AppTypography.Body
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && budget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除预算") },
            text = { Text("确定要删除预算「${budget.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke(budget)
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
