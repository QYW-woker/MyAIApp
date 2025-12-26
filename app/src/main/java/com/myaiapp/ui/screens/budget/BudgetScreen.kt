package com.myaiapp.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = viewModel(factory = BudgetViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "预算管理",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加预算")
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
            // 总预算概览
            item {
                TotalBudgetCard(
                    totalBudget = uiState.totalBudget,
                    totalSpent = uiState.totalSpent,
                    remaining = uiState.remaining
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }

            // 分类预算列表
            item {
                Text(
                    text = "分类预算",
                    style = AppTypography.Title3,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
            }

            if (uiState.budgets.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Add,
                        title = "暂无预算",
                        subtitle = "点击右上角添加预算",
                        actionText = "添加预算",
                        onAction = { showAddDialog = true }
                    )
                }
            } else {
                items(uiState.budgets) { budgetItem ->
                    BudgetItem(
                        name = budgetItem.name,
                        categoryName = budgetItem.categoryName,
                        categoryIcon = budgetItem.categoryIcon,
                        categoryColor = budgetItem.categoryColor,
                        spent = budgetItem.spent,
                        budget = budgetItem.budget,
                        onClick = { /* TODO: Edit budget */ }
                    )
                }
            }
        }
    }

    // 添加预算对话框
    if (showAddDialog) {
        AddBudgetDialog(
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, categoryId, amount ->
                viewModel.addBudget(name, categoryId, amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TotalBudgetCard(
    totalBudget: Double,
    totalSpent: Double,
    remaining: Double
) {
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = totalSpent > totalBudget

    AppCard(
        modifier = Modifier.padding(AppDimens.SpaceLG)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "本月预算",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(totalBudget),
                style = AppTypography.AmountLarge,
                color = AppColors.Gray900
            )

            Spacer(modifier = Modifier.height(16.dp))

            GradientProgressBar(
                progress = progress,
                colors = if (isOverBudget) {
                    listOf(AppColors.Red, AppColors.Orange)
                } else {
                    listOf(AppColors.Blue, AppColors.Purple)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("已花费", style = AppTypography.Caption, color = AppColors.Gray500)
                    Text(
                        formatAmount(totalSpent),
                        style = AppTypography.BodyBold,
                        color = AppColors.Gray900
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (isOverBudget) "超支" else "剩余",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        formatAmount(kotlin.math.abs(remaining)),
                        style = AppTypography.BodyBold,
                        color = if (isOverBudget) AppColors.Red else AppColors.Green
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetItem(
    name: String,
    categoryName: String?,
    categoryIcon: String,
    categoryColor: String,
    spent: Double,
    budget: Double,
    onClick: () -> Unit
) {
    val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = spent > budget

    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.SpaceLG)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(
                    icon = categoryIcon,
                    color = parseColor(categoryColor),
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                    )
                    if (categoryName != null) {
                        Text(
                            text = categoryName,
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatAmount(spent)} / ${formatAmount(budget)}",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = AppTypography.BodyBold,
                        color = if (isOverBudget) AppColors.Red else AppColors.Blue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GradientProgressBar(
                progress = progress,
                height = 6.dp,
                colors = if (isOverBudget) {
                    listOf(AppColors.Red, AppColors.Orange)
                } else {
                    listOf(parseColor(categoryColor), parseColor(categoryColor).copy(alpha = 0.7f))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    categories: List<BudgetCategory>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, categoryId: String?, amount: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加预算") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("预算名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("预算金额") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val budgetAmount = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && budgetAmount > 0) {
                        onConfirm(name, selectedCategoryId, budgetAmount)
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
