package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.myaiapp.data.local.model.Reminder
import com.myaiapp.data.local.model.ReminderType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 提醒设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: ReminderSettingsViewModel = viewModel(
        factory = ReminderSettingsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddReminderSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "记账提醒",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddReminderSheet = true },
                containerColor = AppColors.Blue
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "添加提醒",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 每日记账提醒
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = AppColors.Blue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "每日记账提醒",
                                        style = AppTypography.Body,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "每天提醒你记录当日支出",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.dailyReminderEnabled,
                                onCheckedChange = { viewModel.setDailyReminderEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = AppColors.Green,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }

                        if (uiState.dailyReminderEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = AppColors.Gray100)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "提醒时间",
                                    style = AppTypography.Body
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(
                                            "%02d:%02d",
                                            uiState.dailyReminderHour,
                                            uiState.dailyReminderMinute
                                        ),
                                        style = AppTypography.Title3,
                                        color = AppColors.Blue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = AppColors.Gray400
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 预算超支提醒
            item {
                AppCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = AppColors.Orange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "预算超支提醒",
                                    style = AppTypography.Body,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "预算使用超过80%时提醒",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray500
                                )
                            }
                        }
                        Switch(
                            checked = uiState.budgetAlertEnabled,
                            onCheckedChange = { viewModel.setBudgetAlertEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AppColors.Green,
                                checkedThumbColor = Color.White
                            )
                        )
                    }
                }
            }

            // 存钱提醒
            item {
                AppCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Savings,
                                contentDescription = null,
                                tint = AppColors.Green,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "存钱提醒",
                                    style = AppTypography.Body,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "定期提醒你完成存钱计划",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray500
                                )
                            }
                        }
                        Switch(
                            checked = uiState.savingsReminderEnabled,
                            onCheckedChange = { viewModel.setSavingsReminderEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AppColors.Green,
                                checkedThumbColor = Color.White
                            )
                        )
                    }
                }
            }

            // 自定义提醒列表
            if (uiState.customReminders.isNotEmpty()) {
                item {
                    Text(
                        text = "自定义提醒",
                        style = AppTypography.Subhead,
                        color = AppColors.Gray500
                    )
                }

                items(uiState.customReminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onToggle = { viewModel.toggleReminder(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }
        }
    }

    // 时间选择器
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.dailyReminderHour,
            initialMinute = uiState.dailyReminderMinute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择提醒时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDailyReminderTime(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 添加提醒弹窗
    if (showAddReminderSheet) {
        AddReminderSheet(
            onDismiss = { showAddReminderSheet = false },
            onAdd = { title, type, time, repeatType ->
                viewModel.addReminder(title, type, time, repeatType)
                showAddReminderSheet = false
            }
        )
    }
}

@Composable
private fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (reminder.type) {
                    ReminderType.CREDIT_CARD -> Icons.Outlined.CreditCard
                    ReminderType.SAVINGS -> Icons.Outlined.Savings
                    ReminderType.BUDGET -> Icons.Outlined.AccountBalance
                    else -> Icons.Outlined.Notifications
                },
                contentDescription = null,
                tint = if (reminder.isEnabled) AppColors.Blue else AppColors.Gray400,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = AppTypography.Body,
                    fontWeight = FontWeight.Medium,
                    color = if (reminder.isEnabled) AppColors.Gray900 else AppColors.Gray400
                )
                Row {
                    Text(
                        text = dateFormat.format(Date(reminder.time)),
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    if (reminder.repeatType != "NONE") {
                        Text(
                            text = " · ${
                                when (reminder.repeatType) {
                                    "DAILY" -> "每天"
                                    "WEEKLY" -> "每周"
                                    "MONTHLY" -> "每月"
                                    else -> ""
                                }
                            }",
                            style = AppTypography.Caption,
                            color = AppColors.Blue
                        )
                    }
                }
            }

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AppColors.Green,
                    checkedThumbColor = Color.White
                )
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Gray400
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderSheet(
    onDismiss: () -> Unit,
    onAdd: (String, ReminderType, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.CUSTOM) }
    var selectedRepeat by remember { mutableStateOf("NONE") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "添加提醒",
                style = AppTypography.Title2,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("提醒标题") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 提醒类型
            Text(
                text = "提醒类型",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderType.values().filter { it != ReminderType.RECORD }.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                when (type) {
                                    ReminderType.CREDIT_CARD -> "还款"
                                    ReminderType.SAVINGS -> "存钱"
                                    ReminderType.BUDGET -> "预算"
                                    ReminderType.DEBT -> "债务"
                                    else -> "其他"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 日期时间选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            .format(Date(selectedDate))
                    )
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(String.format("%02d:%02d", selectedHour, selectedMinute))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 重复设置
            Text(
                text = "重复",
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("NONE" to "不重复", "DAILY" to "每天", "WEEKLY" to "每周", "MONTHLY" to "每月")
                    .forEach { (value, label) ->
                        FilterChip(
                            selected = selectedRepeat == value,
                            onClick = { selectedRepeat = value },
                            label = { Text(label) }
                        )
                    }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 确认按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                        }
                        onAdd(title, selectedType, calendar.timeInMillis, selectedRepeat)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue)
            ) {
                Text("添加提醒", fontWeight = FontWeight.Medium)
            }
        }

        // 日期选择器
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate = it }
                            showDatePicker = false
                        }
                    ) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // 时间选择器
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedHour,
                initialMinute = selectedMinute
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("选择时间") },
                text = { TimePicker(state = timePickerState) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                            showTimePicker = false
                        }
                    ) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("取消") }
                }
            )
        }
    }
}
