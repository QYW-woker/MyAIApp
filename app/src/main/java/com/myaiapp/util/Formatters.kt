package com.myaiapp.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 格式化工具
 */

// 金额格式化
private val amountFormatter = DecimalFormat("#,##0.00")
private val numberFormatter = DecimalFormat("#,##0")

fun formatAmount(amount: Double): String {
    return "¥${amountFormatter.format(amount)}"
}

fun formatNumber(number: Double): String {
    return if (number == number.toLong().toDouble()) {
        numberFormatter.format(number.toLong())
    } else {
        amountFormatter.format(number)
    }
}

// 日期格式化
private val dateFormatter = SimpleDateFormat("MM月dd日", Locale.CHINA)
private val fullDateFormatter = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
private val timeFormatter = SimpleDateFormat("HH:mm", Locale.CHINA)
private val monthFormatter = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
private val dayOfWeekFormatter = SimpleDateFormat("EEEE", Locale.CHINA)

fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val todayYear = calendar.get(Calendar.YEAR)
    val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = timestamp
    val dateYear = calendar.get(Calendar.YEAR)
    val dateDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    return when {
        todayYear == dateYear && todayDayOfYear == dateDayOfYear -> "今天"
        todayYear == dateYear && todayDayOfYear - dateDayOfYear == 1 -> "昨天"
        todayYear == dateYear -> dateFormatter.format(Date(timestamp))
        else -> fullDateFormatter.format(Date(timestamp))
    }
}

fun formatFullDate(timestamp: Long): String {
    return fullDateFormatter.format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    return timeFormatter.format(Date(timestamp))
}

fun formatMonth(timestamp: Long): String {
    return monthFormatter.format(Date(timestamp))
}

fun formatDayOfWeek(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val todayYear = calendar.get(Calendar.YEAR)
    val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = timestamp
    val dateYear = calendar.get(Calendar.YEAR)
    val dateDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    return when {
        todayYear == dateYear && todayDayOfYear == dateDayOfYear -> "今天"
        todayYear == dateYear && todayDayOfYear - dateDayOfYear == 1 -> "昨天"
        todayYear == dateYear && dateDayOfYear - todayDayOfYear == 1 -> "明天"
        else -> dayOfWeekFormatter.format(Date(timestamp))
    }
}

// 获取指定月份的开始时间戳
fun getMonthStartTimestamp(year: Int, month: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// 获取指定月份的结束时间戳
fun getMonthEndTimestamp(year: Int, month: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1, 23, 59, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    return calendar.timeInMillis
}

// 获取今天的开始时间戳
fun getTodayStartTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// 获取今天的结束时间戳
fun getTodayEndTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

// 获取本周的开始时间戳
fun getWeekStartTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// 获取本月的开始时间戳
fun getCurrentMonthStartTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// 获取本年的开始时间戳
fun getCurrentYearStartTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, Calendar.JANUARY)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

// 判断两个时间戳是否在同一天
fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// 判断两个时间戳是否在同一月
fun isSameMonth(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

// 获取日期的年月日
fun getDateParts(timestamp: Long): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return Triple(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
