package com.myaiapp.ui.screens.savings

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
import com.myaiapp.data.local.model.SavingsPlan
import com.myaiapp.data.local.model.SavingsRecord
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import java.text.SimpleDateFormat
import java.util.*

/**
 * 存钱计划编辑底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsEditSheet(
    plan: SavingsPlan? = null,
    onDismiss: () -> Unit,
    onSave: (SavingsPlan) -> Unit,
    onDelete: ((SavingsPlan) -> Unit)? = null
) {
    var name by remember { mutableStateOf(plan?.name ?: "") }
    var targetAmount by remember { mutableStateOf(plan?.targetAmount?.toString() ?: "") }
    var selectedEmoji by remember { mutableStateOf(plan?.emoji ?: "💰") }
    var note by remember { mutableStateOf(plan?.note ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = plan != null

    val emojis = listOf(
        "💰", "🏠", "🚗", "✈️", "💻", "📱", "👗", "💍", "🎓", "🎁",
        "🎮", "📷", "⌚", "🏖️", "🎵", "🐱", "🐕", "💄", "👶", "🏋️"
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
                    text = if (isEditing) "编辑计划" else "新建存钱计划",
                    style = AppTypography.Title2
                )
                if (isEditing && onDelete != null) {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("删除", color = AppColors.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 计划名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("计划名称") },
                placeholder = { Text("如：旅行基金、新手机") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 目标金额
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("目标金额") },
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

            // 选择图标
            Text(
                text = "选择图标",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 第一行图标
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojis.take(10)) { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedEmoji = emoji },
                        shape = RoundedCornerShape(12.dp),
                        color = if (emoji == selectedEmoji) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, style = AppTypography.Title2)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第二行图标
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojis.drop(10)) { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedEmoji = emoji },
                        shape = RoundedCornerShape(12.dp),
                        color = if (emoji == selectedEmoji) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, style = AppTypography.Title2)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Gray200,
                    focusedBorderColor = AppColors.Blue
                )
            )

            // 显示当前进度（编辑模式）
            if (isEditing && plan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AppCard {
                    Column(
                        modifier = Modifier.padding(AppDimens.CardPadding)
                    ) {
                        Text(
                            text = "当前进度",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = if (plan.targetAmount > 0) {
                            (plan.currentAmount / plan.targetAmount).coerceIn(0.0, 1.0).toFloat()
                        } else 0f
                        GradientProgressBar(
                            progress = progress,
                            colors = listOf(AppColors.Green, AppColors.Blue)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "已存 ${formatAmount(plan.currentAmount)}",
                                style = AppTypography.Body,
                                color = AppColors.Green
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = AppTypography.BodyBold,
                                color = AppColors.Blue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    val target = targetAmount.toDoubleOrNull() ?: 0.0
                    val newPlan = SavingsPlan(
                        id = plan?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        emoji = selectedEmoji,
                        targetAmount = target,
                        currentAmount = plan?.currentAmount ?: 0.0,
                        note = note.takeIf { it.isNotBlank() },
                        isCompleted = plan?.isCompleted ?: false,
                        createdAt = plan?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(newPlan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Blue
                )
            ) {
                Text(
                    text = if (isEditing) "保存修改" else "创建计划",
                    style = AppTypography.Body
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && plan != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除计划") },
            text = { Text("确定要删除存钱计划「${plan.name}」吗？\n\n已存入的记录也将被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke(plan)
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

/**
 * 存入/取出弹窗
 */
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
    var selectedTab by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

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
            // 计划信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(plan.emoji, style = AppTypography.LargeTitle)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = AppTypography.Title2
                    )
                    Text(
                        text = "${formatAmount(plan.currentAmount)} / ${formatAmount(plan.targetAmount)}",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
                IconButton(onClick = onEditPlan) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑",
                        tint = AppColors.Gray400
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 进度条
            val progress = if (plan.targetAmount > 0) {
                (plan.currentAmount / plan.targetAmount).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            GradientProgressBar(
                progress = progress,
                colors = listOf(AppColors.Green, AppColors.Blue)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab切换
            SegmentedControl(
                items = listOf("存入", "取出", "记录"),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // 存入
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("存入金额") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = { Text("¥", color = AppColors.Gray500) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Green
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Green
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 快捷金额
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("100", "500", "1000", "2000").forEach { quickAmount ->
                            OutlinedButton(
                                onClick = { amount = quickAmount },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("¥$quickAmount", style = AppTypography.Caption)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val depositAmount = amount.toDoubleOrNull() ?: 0.0
                            if (depositAmount > 0) {
                                onDeposit(depositAmount, note)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Green
                        )
                    ) {
                        Text("存入", style = AppTypography.Body)
                    }
                }

                1 -> {
                    // 取出
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("取出金额") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = { Text("¥", color = AppColors.Gray500) },
                        supportingText = {
                            Text("可取出: ${formatAmount(plan.currentAmount)}", color = AppColors.Gray500)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Orange
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Orange
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val withdrawAmount = amount.toDoubleOrNull() ?: 0.0
                            if (withdrawAmount > 0 && withdrawAmount <= plan.currentAmount) {
                                onWithdraw(withdrawAmount, note)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                                (amount.toDoubleOrNull() ?: 0.0) <= plan.currentAmount,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Orange
                        )
                    ) {
                        Text("取出", style = AppTypography.Body)
                    }
                }

                2 -> {
                    // 存取记录
                    if (records.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = AppColors.Gray300,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "暂无存取记录",
                                    style = AppTypography.Body,
                                    color = AppColors.Gray400
                                )
                            }
                        }
                    } else {
                        records.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (record.amount > 0) {
                                        Icons.Outlined.Add
                                    } else {
                                        Icons.Outlined.Remove
                                    },
                                    contentDescription = null,
                                    tint = if (record.amount > 0) AppColors.Green else AppColors.Orange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.note ?: if (record.amount > 0) "存入" else "取出",
                                        style = AppTypography.Body
                                    )
                                    Text(
                                        text = dateFormat.format(Date(record.date)),
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }
                                Text(
                                    text = "${if (record.amount > 0) "+" else ""}${formatAmount(record.amount)}",
                                    style = AppTypography.BodyBold,
                                    color = if (record.amount > 0) AppColors.Green else AppColors.Orange
                                )
                            }
                            Divider(color = AppColors.Gray100)
                        }
                    }
                }
            }
        }
    }
}
