package com.myaiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaiapp.data.local.model.SavingsPlan
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatNumber

/**
 * 存钱计划卡片
 */
@Composable
fun SavingsCard(
    plan: SavingsPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (plan.targetAmount > 0) {
        (plan.currentAmount / plan.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val isCompleted = plan.currentAmount >= plan.targetAmount

    val gradientColors = if (isCompleted) {
        listOf(Color(0xFF4CD964), Color(0xFF34C759))
    } else {
        listOf(Color(0xFF10B981), Color(0xFF059669))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = gradientColors))
                .padding(24.dp)
        ) {
            Column {
                // 头部：emoji + 名称 + 状态标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = plan.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = plan.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 状态标签
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isCompleted) "已完成" else "进行中",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(Color.White, RoundedCornerShape(10.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部金额信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "¥${formatNumber(plan.currentAmount)}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "目标 ¥${formatNumber(plan.targetAmount)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 存钱计划小卡片（用于首页展示）
 */
@Composable
fun SavingsCardCompact(
    plan: SavingsPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (plan.targetAmount > 0) {
        (plan.currentAmount / plan.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    AppCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.emoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = plan.name,
                        style = AppTypography.Title3
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = AppTypography.Subhead,
                    color = AppColors.Green
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GradientProgressBar(
                progress = progress,
                height = 8.dp,
                colors = listOf(AppColors.Green, AppColors.Teal)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已存 ¥${formatNumber(plan.currentAmount)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
                Text(
                    text = "目标 ¥${formatNumber(plan.targetAmount)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

/**
 * 存钱总览卡片
 */
@Composable
fun SavingsTotalCard(
    totalSaved: Double,
    totalTarget: Double,
    plansCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTarget > 0) {
        (totalSaved / totalTarget).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "累计已存",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¥${formatNumber(totalSaved)}",
                style = AppTypography.AmountLarge,
                color = AppColors.Green
            )

            Spacer(modifier = Modifier.height(16.dp))

            GradientProgressBar(
                progress = progress,
                colors = listOf(AppColors.Green, AppColors.Teal)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${plansCount}个进行中的计划",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
                Text(
                    text = "目标 ¥${formatNumber(totalTarget)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }
}
