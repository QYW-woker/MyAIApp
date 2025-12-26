package com.myaiapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "账单日历",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 月份选择器
            MonthSelector(
                monthLabel = uiState.monthLabel,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )

            // 月度汇总
            SummaryCard(
                income = uiState.monthIncome,
                expense = uiState.monthExpense,
                balance = uiState.monthBalance,
                modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
            )

            // 日历网格
            CalendarGrid(
                days = uiState.calendarDays,
                selectedDay = uiState.selectedDay,
                onDaySelected = { viewModel.selectDay(it) }
            )

            Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

            // 选中日期的交易列表
            Text(
                text = uiState.selectedDayLabel,
                style = AppTypography.Title3,
                modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = AppDimens.SpaceSM)
            ) {
                items(uiState.selectedDayTransactions) { transaction ->
                    val category = uiState.categories.find { it.id == transaction.categoryId }
                    if (category != null) {
                        Surface(color = Color.White) {
                            TransactionItem(
                                transaction = transaction,
                                category = category,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                        Divider(
                            modifier = Modifier.padding(start = 74.dp),
                            color = AppColors.Gray100
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.SpaceLG),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "上月", tint = AppColors.Gray600)
        }
        Text(
            text = monthLabel,
            style = AppTypography.Title2,
            modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "下月", tint = AppColors.Gray600)
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit
) {
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG)
    ) {
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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

        // 日历格子
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { day ->
                CalendarDayCell(
                    day = day,
                    isSelected = day.dayOfMonth == selectedDay && day.isCurrentMonth,
                    onClick = {
                        if (day.isCurrentMonth && day.dayOfMonth != null) {
                            onDaySelected(day.dayOfMonth)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasData = day.income > 0 || day.expense > 0

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isSelected) {
                    Modifier.background(AppColors.Blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (day.dayOfMonth != null) {
            Text(
                text = day.dayOfMonth.toString(),
                style = AppTypography.Body,
                color = when {
                    !day.isCurrentMonth -> AppColors.Gray300
                    day.isToday -> AppColors.Blue
                    isSelected -> AppColors.Blue
                    else -> AppColors.Gray900
                },
                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasData && day.isCurrentMonth) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (day.expense > 0) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(AppColors.Orange, CircleShape)
                        )
                    }
                    if (day.income > 0) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(AppColors.Green, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

data class CalendarDay(
    val dayOfMonth: Int?,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val income: Double = 0.0,
    val expense: Double = 0.0
)
