package com.myaiapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatDate
import com.myaiapp.util.formatDayOfWeek

@Composable
fun HomeScreen(
    onNavigateToRecords: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddRecord: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 顶部标题栏
        item {
            LargeTitleTopBar(
                title = uiState.currentBookName,
                subtitle = uiState.monthLabel,
                actions = {
                    CircleIconButton(
                        icon = Icons.Outlined.CalendarMonth,
                        onClick = onNavigateToCalendar
                    )
                    CircleIconButton(
                        icon = Icons.Outlined.Settings,
                        onClick = onNavigateToSettings
                    )
                }
            )
        }

        // 收支汇总卡片
        item {
            SummaryCard(
                income = uiState.monthIncome,
                expense = uiState.monthExpense,
                balance = uiState.monthBalance,
                modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
            )
            Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
        }

        // 快捷操作
        item {
            QuickActions(
                onBudgetClick = onNavigateToBudget,
                onSavingsClick = onNavigateToSavings,
                onStatisticsClick = onNavigateToStatistics,
                onCalendarClick = onNavigateToCalendar
            )
            Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
        }

        // 预算进度（如果有）
        if (uiState.budgetProgress != null) {
            item {
                SectionHeader(title = "本月预算", onMoreClick = onNavigateToBudget)
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
                BudgetProgressCard(
                    title = uiState.budgetProgress!!.name,
                    spent = uiState.budgetProgress!!.spent,
                    total = uiState.budgetProgress!!.total,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG),
                    onClick = onNavigateToBudget
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }
        }

        // 存钱计划（如果有）
        if (uiState.savingsPlans.isNotEmpty()) {
            item {
                SectionHeader(title = "存钱计划", onMoreClick = onNavigateToSavings)
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AppDimens.SpaceLG),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.savingsPlans.take(3)) { plan ->
                        SavingsCardCompact(
                            plan = plan,
                            onClick = onNavigateToSavings,
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }
        }

        // 最近记录
        item {
            SectionHeader(title = "最近记录", onMoreClick = onNavigateToRecords)
            Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = "暂无记录",
                    subtitle = "点击下方按钮开始记账",
                    actionText = "立即记账",
                    onAction = onAddRecord
                )
            }
        } else {
            // 按日期分组显示
            val groupedTransactions = uiState.recentTransactions.groupBy { formatDate(it.date) }

            groupedTransactions.forEach { (date, transactions) ->
                val dayIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val dayExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                item {
                    DateHeader(
                        date = date,
                        dayOfWeek = formatDayOfWeek(transactions.first().date),
                        income = dayIncome,
                        expense = dayExpense
                    )
                }

                items(transactions) { transaction ->
                    val category = uiState.categories.find { it.id == transaction.categoryId }
                    if (category != null) {
                        Surface(
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                        ) {
                            TransactionItem(
                                transaction = transaction,
                                category = category,
                                onClick = { /* TODO: Navigate to detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.Title3,
            color = AppColors.Gray900
        )
        TextButton(onClick = onMoreClick) {
            Text(
                text = "查看全部",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppColors.Gray400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickActions(
    onBudgetClick: () -> Unit,
    onSavingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem(
            icon = Icons.Outlined.AccountBalanceWallet,
            label = "预算",
            color = AppColors.Blue,
            onClick = onBudgetClick
        )
        QuickActionItem(
            icon = Icons.Outlined.Savings,
            label = "存钱",
            color = AppColors.Green,
            onClick = onSavingsClick
        )
        QuickActionItem(
            icon = Icons.Outlined.PieChart,
            label = "统计",
            color = AppColors.Purple,
            onClick = onStatisticsClick
        )
        QuickActionItem(
            icon = Icons.Outlined.CalendarMonth,
            label = "日历",
            color = AppColors.Orange,
            onClick = onCalendarClick
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.Gray600
        )
    }
}
