package com.myaiapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount

/**
 * 收支汇总卡片
 */
@Composable
fun SummaryCard(
    income: Double,
    expense: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                label = "收入",
                amount = income,
                color = AppColors.Green
            )

            VerticalDivider()

            SummaryItem(
                label = "支出",
                amount = expense,
                color = AppColors.Gray900
            )

            VerticalDivider()

            SummaryItem(
                label = "结余",
                amount = balance,
                color = AppColors.Blue
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.Gray500
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatAmount(amount),
            style = AppTypography.AmountMedium,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VerticalDivider() {
    Divider(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp),
        color = AppColors.Gray100
    )
}

/**
 * 预算进度卡片
 */
@Composable
fun BudgetProgressCard(
    title: String,
    spent: Double,
    total: Double,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val progress = if (total > 0) (spent / total).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = spent > total
    val progressColors = if (isOverBudget) {
        listOf(AppColors.Red, AppColors.Orange)
    } else {
        listOf(AppColors.Blue, AppColors.Purple)
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = title,
                    style = AppTypography.Title3
                )
                Text(
                    text = "${formatAmount(spent)} / ${formatAmount(total)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            GradientProgressBar(
                progress = progress,
                colors = progressColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已用 ${(progress * 100).toInt()}%",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
                Text(
                    text = if (isOverBudget) {
                        "超支 ${formatAmount(spent - total)}"
                    } else {
                        "剩余 ${formatAmount(total - spent)}"
                    },
                    style = AppTypography.Caption,
                    color = if (isOverBudget) AppColors.Red else AppColors.Gray500
                )
            }
        }
    }
}

/**
 * 资产总览卡片
 */
@Composable
fun AssetOverviewCard(
    totalAssets: Double,
    totalLiabilities: Double,
    netWorth: Double,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.CardPadding)
        ) {
            Text(
                text = "净资产",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(netWorth),
                style = AppTypography.AmountLarge,
                color = if (netWorth >= 0) AppColors.Blue else AppColors.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总资产",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        text = formatAmount(totalAssets),
                        style = AppTypography.AmountSmall,
                        color = AppColors.Green
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "总负债",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        text = formatAmount(totalLiabilities),
                        style = AppTypography.AmountSmall,
                        color = AppColors.Red
                    )
                }
            }
        }
    }
}
