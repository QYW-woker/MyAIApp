package com.myaiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myaiapp.ui.theme.*
import java.util.*

/**
 * 日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var currentCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDate })
    }
    var selectedDay by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDate }.get(Calendar.DAY_OF_MONTH))
    }

    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 计算当月第一天是周几
    val firstDayCalendar = currentCalendar.clone() as Calendar
    firstDayCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = (firstDayCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column {
                // 月份导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentCalendar = (currentCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }) {
                        Icon(Icons.Outlined.ChevronLeft, "上月", tint = AppColors.Gray600)
                    }

                    Text(
                        text = "${year}年${month + 1}月",
                        style = AppTypography.Title3
                    )

                    IconButton(onClick = {
                        currentCalendar = (currentCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    }) {
                        Icon(Icons.Outlined.ChevronRight, "下月", tint = AppColors.Gray600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 星期标题
                val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            style = AppTypography.Caption,
                            color = AppColors.Gray500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 日期网格
                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7

                Column {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val day = cellIndex - firstDayOfWeek + 1

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day in 1..daysInMonth) {
                                        val isSelected = day == selectedDay
                                        val isToday = isCurrentMonth && day == today.get(Calendar.DAY_OF_MONTH)

                                        Surface(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable { selectedDay = day },
                                            shape = CircleShape,
                                            color = when {
                                                isSelected -> AppColors.Blue
                                                isToday -> AppColors.Blue.copy(alpha = 0.1f)
                                                else -> Color.Transparent
                                            }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = day.toString(),
                                                    style = AppTypography.Body,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> AppColors.Blue
                                                        else -> AppColors.Gray900
                                                    },
                                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val resultCalendar = Calendar.getInstance().apply {
                        set(year, month, selectedDay, 12, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(resultCalendar.timeInMillis)
                }
            ) {
                Text("确定", color = AppColors.Blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.Gray500)
            }
        }
    )
}

/**
 * 快捷日期选择
 */
@Composable
fun QuickDateSelector(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val isToday = isSameDay(selectedCal, today)
    val isYesterday = isSameDay(selectedCal, yesterday)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickDateChip(
            text = "今天",
            isSelected = isToday,
            onClick = {
                today.set(Calendar.HOUR_OF_DAY, 12)
                onDateSelected(today.timeInMillis)
            }
        )
        QuickDateChip(
            text = "昨天",
            isSelected = isYesterday,
            onClick = {
                yesterday.set(Calendar.HOUR_OF_DAY, 12)
                onDateSelected(yesterday.timeInMillis)
            }
        )
    }
}

@Composable
private fun QuickDateChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AppColors.Blue else AppColors.Gray100
    ) {
        Text(
            text = text,
            style = AppTypography.Caption,
            color = if (isSelected) Color.White else AppColors.Gray600,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
