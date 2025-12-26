package com.myaiapp.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.ai.FinanceAnalyzer
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "智能分析",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppColors.Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在分析您的财务数据...", style = AppTypography.Body, color = AppColors.Gray500)
                }
            }
        } else if (uiState.analysisResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AppColors.Gray300
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无足够数据进行分析", style = AppTypography.Body, color = AppColors.Gray500)
                    Text("记录更多账目后再来看看吧", style = AppTypography.Caption, color = AppColors.Gray400)
                }
            }
        } else {
            val result = uiState.analysisResult!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 健康分数卡片
                item {
                    HealthScoreCard(
                        score = result.healthScore,
                        savingsRate = result.summary.savingsRate
                    )
                    Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                }

                // 消费摘要
                item {
                    SpendingSummaryCard(summary = result.summary)
                    Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                }

                // 消费趋势
                if (result.trends.isNotEmpty()) {
                    item {
                        TrendsCard(trends = result.trends)
                        Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                    }
                }

                // 洞察标题
                if (result.insights.isNotEmpty()) {
                    item {
                        Text(
                            text = "财务洞察",
                            style = AppTypography.Title3,
                            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
                        )
                    }

                    items(result.insights) { insight ->
                        InsightCard(insight = insight)
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.SpaceLG))
                    }
                }

                // 建议标题
                if (result.suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "优化建议",
                            style = AppTypography.Title3,
                            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
                        )
                    }

                    items(result.suggestions) { suggestion ->
                        SuggestionCard(suggestion = suggestion)
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthScoreCard(
    score: Int,
    savingsRate: Double
) {
    val scoreColor = when {
        score >= 80 -> AppColors.Green
        score >= 60 -> AppColors.Orange
        else -> AppColors.Red
    }

    val scoreText = when {
        score >= 80 -> "优秀"
        score >= 60 -> "良好"
        score >= 40 -> "一般"
        else -> "需改善"
    }

    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "财务健康度",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 圆形分数指示器
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = AppTypography.AmountLarge,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scoreText,
                        style = AppTypography.Caption,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", savingsRate * 100)}%",
                        style = AppTypography.Title2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "储蓄率",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingSummaryCard(summary: FinanceAnalyzer.SpendingSummary) {
    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "本月概览",
                style = AppTypography.Title3,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("收入", style = AppTypography.Caption, color = AppColors.Gray500)
                    Text(
                        text = formatAmount(summary.totalIncome),
                        style = AppTypography.BodyBold,
                        color = AppColors.Green
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("支出", style = AppTypography.Caption, color = AppColors.Gray500)
                    Text(
                        text = formatAmount(summary.totalExpense),
                        style = AppTypography.BodyBold,
                        color = AppColors.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = AppColors.Gray100)

            Spacer(modifier = Modifier.height(16.dp))

            // 分类消费
            Text(
                text = "支出构成",
                style = AppTypography.Subhead,
                color = AppColors.Gray600
            )

            Spacer(modifier = Modifier.height(8.dp))

            summary.topCategories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.categoryName,
                        style = AppTypography.Body,
                        modifier = Modifier.weight(1f)
                    )

                    // 趋势指示
                    when (category.trend) {
                        FinanceAnalyzer.TrendDirection.UP -> Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.Red
                        )
                        FinanceAnalyzer.TrendDirection.DOWN -> Icon(
                            imageVector = Icons.Outlined.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.Green
                        )
                        else -> {}
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${String.format("%.0f", category.percentage * 100)}%",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatAmount(category.amount),
                        style = AppTypography.BodyBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendsCard(trends: List<FinanceAnalyzer.Trend>) {
    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "消费趋势",
                style = AppTypography.Title3,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 简易柱状图
            val maxValue = trends.maxOfOrNull { it.value } ?: 1.0

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(trends) { trend ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        val barHeight = if (maxValue > 0) (trend.value / maxValue * 80).dp else 0.dp

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (trend.direction) {
                                        FinanceAnalyzer.TrendDirection.UP -> AppColors.Red.copy(alpha = 0.7f)
                                        FinanceAnalyzer.TrendDirection.DOWN -> AppColors.Green.copy(alpha = 0.7f)
                                        else -> AppColors.Primary.copy(alpha = 0.7f)
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = trend.period,
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: FinanceAnalyzer.Insight) {
    val iconColor = when (insight.importance) {
        FinanceAnalyzer.Importance.HIGH -> AppColors.Red
        FinanceAnalyzer.Importance.MEDIUM -> AppColors.Orange
        FinanceAnalyzer.Importance.LOW -> AppColors.Green
    }

    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.SpaceLG),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (insight.icon) {
                        "trending_up" -> Icons.Outlined.TrendingUp
                        "trending_down" -> Icons.Outlined.TrendingDown
                        "warning" -> Icons.Outlined.Warning
                        "savings" -> Icons.Outlined.Savings
                        "emoji_events" -> Icons.Outlined.EmojiEvents
                        "priority_high" -> Icons.Outlined.PriorityHigh
                        else -> Icons.Outlined.Lightbulb
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = insight.description,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: FinanceAnalyzer.Suggestion) {
    val priorityColor = when (suggestion.priority) {
        FinanceAnalyzer.Priority.HIGH -> AppColors.Red
        FinanceAnalyzer.Priority.MEDIUM -> AppColors.Orange
        FinanceAnalyzer.Priority.LOW -> AppColors.Primary
    }

    AppCard(
        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.SpaceLG),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(priorityColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (suggestion.type) {
                        FinanceAnalyzer.SuggestionType.REDUCE_SPENDING -> Icons.Outlined.TrendingDown
                        FinanceAnalyzer.SuggestionType.INCREASE_SAVINGS -> Icons.Outlined.Savings
                        FinanceAnalyzer.SuggestionType.BUDGET_ADJUSTMENT -> Icons.Outlined.AccountBalance
                        FinanceAnalyzer.SuggestionType.CATEGORY_LIMIT -> Icons.Outlined.DoNotDisturb
                        FinanceAnalyzer.SuggestionType.FINANCIAL_HABIT -> Icons.Outlined.Lightbulb
                    },
                    contentDescription = null,
                    tint = priorityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = suggestion.description,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )

                suggestion.potentialSavings?.let { savings ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "预计可节省: ${formatAmount(savings)}",
                        style = AppTypography.Caption,
                        color = AppColors.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
