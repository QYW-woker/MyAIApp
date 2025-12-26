package com.myaiapp.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * iOS风格字体规范
 */
object AppTypography {
    // 大标题 (页面标题)
    val LargeTitle = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )

    // 标题1
    val Title1 = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    )

    // 标题2
    val Title2 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )

    // 标题3
    val Title3 = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
    )

    // 正文
    val Body = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )

    // 正文加粗
    val BodyBold = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )

    // 副文本
    val Subhead = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )

    // 注释
    val Caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    )

    // 注释2
    val Caption2 = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    )

    // 金额大字
    val AmountLarge = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )

    // 金额中等
    val AmountMedium = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )

    // 金额小
    val AmountSmall = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
    )

    // 标签页
    val TabLabel = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    // 按钮
    val Button = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}
