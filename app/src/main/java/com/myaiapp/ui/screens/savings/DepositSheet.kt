package com.myaiapp.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaiapp.data.local.model.SavingsPlan
import com.myaiapp.data.local.model.SavingsRecord
import com.myaiapp.ui.components.GradientProgressBar
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import com.myaiapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositSheet(
    plan: SavingsPlan,
    records: List<SavingsRecord>,
    onDismiss: () -> Unit,
    onDeposit: (Double, String) -> Unit,
    onWithdraw: (Double, String) -> Unit,
    onEditPlan: () -> Unit
) {
    var isDeposit by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val progress = if (plan.targetAmount > 0) {
        (plan.currentAmount / plan.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.SpaceLG)
                .padding(bottom = 32.dp)
        ) {
            // 标题栏
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.emoji,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = plan.name,
                            style = AppTypography.Title2
                        )
                    }
                    Row {
                        IconButton(onClick = onEditPlan) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "编辑",
                                tint = AppColors.Gray600
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 进度卡片
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Green.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "已存",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray600
                                )
                                Text(
                                    text = "¥${formatNumber(plan.currentAmount)}",
                                    style = AppTypography.Title2,
                                    color = AppColors.Green
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "目标",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray600
                                )
                                Text(
                                    text = "¥${formatNumber(plan.targetAmount)}",
                                    style = AppTypography.Title3,
                                    color = AppColors.Gray700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GradientProgressBar(
                            progress = progress,
                            height = 10.dp,
                            colors = listOf(AppColors.Green, AppColors.Teal)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "完成 ${(progress * 100).toInt()}%，还差 ¥${formatNumber(plan.targetAmount - plan.currentAmount)}",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 存取切换
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isDeposit = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeposit) AppColors.Green else AppColors.Gray100,
                            contentColor = if (isDeposit) Color.White else AppColors.Gray600
                        )
                    ) {
                        Text("存入")
                    }
                    Button(
                        onClick = { isDeposit = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isDeposit) AppColors.Orange else AppColors.Gray100,
                            contentColor = if (!isDeposit) Color.White else AppColors.Gray600
                        )
                    ) {
                        Text("取出")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 金额输入
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = newValue
                        }
                    },
                    label = { Text(if (isDeposit) "存入金额" else "取出金额") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("¥", style = AppTypography.Body) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDeposit) AppColors.Green else AppColors.Orange,
                        unfocusedBorderColor = AppColors.Gray200
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 备注输入
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    placeholder = { Text("添加备注...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDeposit) AppColors.Green else AppColors.Orange,
                        unfocusedBorderColor = AppColors.Gray200
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 确认按钮
            item {
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0) {
                            if (isDeposit) {
                                onDeposit(amountValue, note)
                            } else {
                                onWithdraw(amountValue, note)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeposit) AppColors.Green else AppColors.Orange
                    ),
                    enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        text = if (isDeposit) "确认存入" else "确认取出",
                        style = AppTypography.Body
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 历史记录
            if (records.isNotEmpty()) {
                item {
                    Text(
                        text = "存取记录",
                        style = AppTypography.Subhead,
                        color = AppColors.Gray600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(records.take(10)) { record ->
                    val isRecordDeposit = record.amount > 0
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isRecordDeposit) "存入" else "取出",
                                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = dateFormat.format(Date(record.date)),
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray500
                                )
                            }
                            Text(
                                text = "${if (isRecordDeposit) "+" else ""}¥${formatNumber(record.amount)}",
                                style = AppTypography.BodyBold,
                                color = if (isRecordDeposit) AppColors.Green else AppColors.Orange
                            )
                        }
                    }
                }
            }
        }
    }
}
