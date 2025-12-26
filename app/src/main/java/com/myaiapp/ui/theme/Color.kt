package com.myaiapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * iOS风格配色系统
 */
object AppColors {
    // 主色调
    val Blue = Color(0xFF5B8DEF)
    val Green = Color(0xFF4CD964)
    val Red = Color(0xFFFF6B6B)
    val Orange = Color(0xFFFFAA5B)
    val Purple = Color(0xFFA78BFA)
    val Pink = Color(0xFFF472B6)
    val Teal = Color(0xFF2DD4BF)
    val Indigo = Color(0xFF818CF8)
    val Cyan = Color(0xFF22D3EE)
    val Yellow = Color(0xFFFBBF24)

    // 语义色
    val Income = Green
    val Expense = Color(0xFF171717)  // 深灰
    val Balance = Blue
    val Warning = Orange
    val Error = Red
    val Success = Green

    // 灰阶
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFE5E5E5)
    val Gray300 = Color(0xFFD4D4D4)
    val Gray400 = Color(0xFFA3A3A3)
    val Gray500 = Color(0xFF737373)
    val Gray600 = Color(0xFF525252)
    val Gray700 = Color(0xFF404040)
    val Gray800 = Color(0xFF262626)
    val Gray900 = Color(0xFF171717)

    // 背景色
    val Background = Gray50
    val Surface = Color.White
    val SurfaceVariant = Gray100

    // 分类颜色列表
    val CategoryColors = listOf(
        Blue, Green, Orange, Purple, Pink,
        Teal, Indigo, Cyan, Red, Yellow
    )
}

// 解析颜色字符串
fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        AppColors.Gray500
    }
}
