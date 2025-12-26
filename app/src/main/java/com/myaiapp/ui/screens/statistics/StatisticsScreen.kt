package com.myaiapp.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import com.myaiapp.util.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "统计",
                onBackClick = onBack
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
            // 时间范围选择
            item {
                SegmentedControl(
                    items = listOf("周", "月", "年"),
                    selectedIndex = uiState.periodIndex,
                    onItemSelected = { viewModel.setPeriod(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
                )
            }

            // 月份/周选择器
            item {
                PeriodSelector(
                    periodLabel = uiState.periodLabel,
                    onPrevious = { viewModel.previousPeriod() },
                    onNext = { viewModel.nextPeriod() }
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
            }

            // 支出/收入切换
            item {
                SegmentedControl(
                    items = listOf("支出", "收入"),
                    selectedIndex = uiState.typeIndex,
                    onItemSelected = { viewModel.setType(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
            }

            // 总金额
            item {
                AppCard(
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.typeIndex == 0) "总支出" else "总收入",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatAmount(uiState.totalAmount),
                            style = AppTypography.AmountLarge,
                            color = if (uiState.typeIndex == 0) AppColors.Gray900 else AppColors.Green
                        )
                        if (uiState.transactionCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "共 ${uiState.transactionCount} 笔，日均 ${formatAmount(uiState.dailyAverage)}",
                                style = AppTypography.Caption,
                                color = AppColors.Gray500
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }

            // 分类占比标题
            item {
                Text(
                    text = "分类占比",
                    style = AppTypography.Title3,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
            }

            // 分类列表
            if (uiState.categoryStats.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.ChevronRight,
                        title = "暂无数据",
                        subtitle = "该时间段内没有记录"
                    )
                }
            } else {
                items(uiState.categoryStats) { stat ->
                    CategoryStatItem(
                        categoryName = stat.categoryName,
                        categoryIcon = stat.categoryIcon,
                        categoryColor = stat.categoryColor,
                        amount = stat.amount,
                        percentage = stat.percentage,
                        count = stat.count
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    periodLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = "上一期",
                tint = AppColors.Gray600
            )
        }

        Text(
            text = periodLabel,
            style = AppTypography.Title3,
            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "下一期",
                tint = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun CategoryStatItem(
    categoryName: String,
    categoryIcon: String,
    categoryColor: String,
    amount: Double,
    percentage: Float,
    count: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp),
        shape = RoundedCornerShape(AppDimens.RadiusMD),
        color = Color.White
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
                        text = categoryName,
                        style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${count}笔",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(amount),
                        style = AppTypography.BodyBold
                    )
                    Text(
                        text = "${formatNumber(percentage.toDouble() * 100)}%",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度条
            GradientProgressBar(
                progress = percentage,
                height = 6.dp,
                colors = listOf(
                    parseColor(categoryColor),
                    parseColor(categoryColor).copy(alpha = 0.7f)
                )
            )
        }
    }
}
