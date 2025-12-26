package com.myaiapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import com.myaiapp.util.formatTime

/**
 * 交易记录列表项
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = androidx.compose.ui.graphics.Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.SpaceLG, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            CategoryIcon(
                icon = category.icon,
                color = parseColor(category.color)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // 中间内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.Gray900
                )
                if (!transaction.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note ?: "",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 金额和时间
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.INCOME -> "+${formatAmount(transaction.amount)}"
                        TransactionType.EXPENSE -> "-${formatAmount(transaction.amount)}"
                        TransactionType.TRANSFER -> formatAmount(transaction.amount)
                    },
                    style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = when (transaction.type) {
                        TransactionType.INCOME -> AppColors.Green
                        TransactionType.EXPENSE -> AppColors.Gray900
                        TransactionType.TRANSFER -> AppColors.Blue
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTime(transaction.date),
                    style = AppTypography.Caption2,
                    color = AppColors.Gray400
                )
            }
        }
    }
}

/**
 * 日期分组头部
 */
@Composable
fun DateHeader(
    date: String,
    dayOfWeek: String,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = AppTypography.Subhead.copy(fontWeight = FontWeight.Medium),
                color = AppColors.Gray900
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dayOfWeek,
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (income > 0) {
                Text(
                    text = "收 ${formatAmount(income)}",
                    style = AppTypography.Caption,
                    color = AppColors.Green
                )
            }
            if (expense > 0) {
                Text(
                    text = "支 ${formatAmount(expense)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

/**
 * 交易详情弹窗中的信息行
 */
@Composable
fun TransactionDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.SpaceSM),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.Body,
            color = AppColors.Gray500
        )
        Text(
            text = value,
            style = AppTypography.Body,
            color = AppColors.Gray900
        )
    }
}
