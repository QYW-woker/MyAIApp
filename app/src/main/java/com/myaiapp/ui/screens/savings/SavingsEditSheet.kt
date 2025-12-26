package com.myaiapp.ui.screens.savings

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
import androidx.compose.ui.unit.sp
import com.myaiapp.data.local.model.SavingsPlan
import com.myaiapp.data.local.model.SavingsType
import com.myaiapp.ui.theme.*
import java.util.UUID

// 预设的emoji列表
private val emojiList = listOf(
    "🏠", "🚗", "✈️", "📱", "💻", "🎮", "📚", "💍",
    "👶", "🎓", "💪", "🏖️", "🎁", "💰", "🏦", "🎯"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsEditSheet(
    onDismiss: () -> Unit,
    onSave: (SavingsPlan) -> Unit,
    plan: SavingsPlan? = null,
    onDelete: ((SavingsPlan) -> Unit)? = null
) {
    val isEdit = plan != null
    var name by remember { mutableStateOf(plan?.name ?: "") }
    var targetAmount by remember { mutableStateOf(plan?.targetAmount?.toString() ?: "") }
    var selectedEmoji by remember { mutableStateOf(plan?.emoji ?: "🎯") }

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
                    text = if (isEdit) "编辑存钱计划" else "添加存钱计划",
                    style = AppTypography.Title2
                )
                Row {
                    if (isEdit && onDelete != null && plan != null) {
                        IconButton(onClick = { onDelete(plan) }) {
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

            // 选择图标
            Text(
                text = "选择图标",
                style = AppTypography.Subhead,
                color = AppColors.Gray600
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojiList) { emoji ->
                    val isSelected = selectedEmoji == emoji
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AppColors.Green.copy(alpha = 0.2f)
                                else AppColors.Gray100
                            )
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 计划名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("计划名称") },
                placeholder = { Text("例如：买房首付") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Green,
                    unfocusedBorderColor = AppColors.Gray200
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 目标金额
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        targetAmount = newValue
                    }
                },
                label = { Text("目标金额") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("¥", style = AppTypography.Body) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Green,
                    unfocusedBorderColor = AppColors.Gray200
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = {
                    val target = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && target > 0) {
                        val newPlan = SavingsPlan(
                            id = plan?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            emoji = selectedEmoji,
                            targetAmount = target,
                            currentAmount = plan?.currentAmount ?: 0.0,
                            type = SavingsType.FLEXIBLE,
                            createdAt = plan?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(newPlan)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Green
                ),
                enabled = name.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = if (isEdit) "保存修改" else "创建计划",
                    style = AppTypography.Body
                )
            }
        }
    }
}
