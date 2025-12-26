package com.myaiapp.ui.screens.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.util.formatMonth
import com.myaiapp.util.isSameDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class CalendarUiState(
    val isLoading: Boolean = true,
    val monthLabel: String = "",
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthBalance: Double = 0.0,
    val calendarDays: List<CalendarDay> = emptyList(),
    val selectedDay: Int? = null,
    val selectedDayLabel: String = "",
    val selectedDayTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList()
)

class CalendarViewModel(
    private val storageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var currentCalendar = Calendar.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val bookId = storageManager.getCurrentBookId()
                allTransactions = storageManager.getTransactions(bookId)
                val categories = storageManager.getCategories()

                _uiState.update { it.copy(categories = categories) }
                updateCalendar()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        updateCalendar()
    }

    fun nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        updateCalendar()
    }

    fun selectDay(day: Int) {
        _uiState.update { it.copy(selectedDay = day) }
        updateSelectedDayTransactions(day)
    }

    private fun updateCalendar() {
        val cal = currentCalendar.clone() as Calendar
        val todayCal = Calendar.getInstance()

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        // 月份标签
        val monthLabel = formatMonth(cal.timeInMillis)

        // 计算月度统计
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val monthEnd = cal.timeInMillis

        val monthTransactions = allTransactions.filter {
            it.date in monthStart..monthEnd
        }

        val monthIncome = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val monthExpense = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // 生成日历格子
        val days = mutableListOf<CalendarDay>()

        // 重置到月初
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 计算需要填充的上月天数
        val leadingDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        // 上月填充
        cal.add(Calendar.DAY_OF_MONTH, -leadingDays)
        repeat(leadingDays) {
            days.add(
                CalendarDay(
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = false
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 当月
        repeat(daysInMonth) { i ->
            val day = i + 1
            cal.set(year, month, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.timeInMillis

            val dayTransactions = monthTransactions.filter {
                it.date in dayStart..dayEnd
            }

            val isToday = todayCal.get(Calendar.YEAR) == year &&
                    todayCal.get(Calendar.MONTH) == month &&
                    todayCal.get(Calendar.DAY_OF_MONTH) == day

            days.add(
                CalendarDay(
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isToday = isToday,
                    income = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                )
            )
        }

        // 下月填充
        val remainingDays = 42 - days.size
        cal.set(year, month, daysInMonth)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        repeat(remainingDays) {
            days.add(
                CalendarDay(
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = false
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 默认选中今天（如果在当月）
        val selectedDay = if (todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == month) {
            todayCal.get(Calendar.DAY_OF_MONTH)
        } else {
            1
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                monthLabel = monthLabel,
                monthIncome = monthIncome,
                monthExpense = monthExpense,
                monthBalance = monthIncome - monthExpense,
                calendarDays = days,
                selectedDay = selectedDay
            )
        }

        updateSelectedDayTransactions(selectedDay)
    }

    private fun updateSelectedDayTransactions(day: Int) {
        val cal = currentCalendar.clone() as Calendar
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        cal.set(year, month, day, 0, 0, 0)
        val dayStart = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val dayEnd = cal.timeInMillis

        val dayTransactions = allTransactions.filter {
            it.date in dayStart..dayEnd
        }.sortedByDescending { it.date }

        val label = "${month + 1}月${day}日"

        _uiState.update {
            it.copy(
                selectedDayLabel = label,
                selectedDayTransactions = dayTransactions
            )
        }
    }
}

class CalendarViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(FileStorageManager(context.applicationContext)) as T
    }
}
