package com.myaiapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaiapp.ui.theme.AppColors
import com.myaiapp.ui.theme.AppTypography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 饼图数据
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * 饼图组件
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 32.dp,
    showLabels: Boolean = true,
    animationDuration: Int = 800
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "pie_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    val total = data.sumOf { it.value.toDouble() }.toFloat()

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            var startAngle = -90f

            data.forEach { slice ->
                val sweepAngle = (slice.value / total) * 360f * animatedProgress

                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    ),
                    topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2),
                    size = Size(
                        size.toPx() - strokeWidth.toPx(),
                        size.toPx() - strokeWidth.toPx()
                    )
                )

                startAngle += sweepAngle
            }
        }

        // 中心文字
        if (showLabels && data.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${data.size}",
                    style = AppTypography.Title1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "分类",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

/**
 * 柱状图数据
 */
data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color = AppColors.Blue
)

/**
 * 柱状图组件
 */
@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    barWidth: Dp = 24.dp,
    barSpacing: Dp = 8.dp,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    animationDuration: Int = 600
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "bar_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 0f

    Column(modifier = modifier) {
        // 柱状图
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { bar ->
                val barHeight = if (maxValue > 0) {
                    (bar.value / maxValue) * animatedProgress
                } else 0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(barWidth + barSpacing)
                ) {
                    if (showValues && bar.value > 0) {
                        Text(
                            text = formatCompactNumber(bar.value),
                            style = AppTypography.Caption2,
                            color = AppColors.Gray500,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .fillMaxHeight(barHeight.coerceIn(0.01f, 1f))
                            .background(
                                bar.color,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }

        // 标签
        if (showLabels) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { bar ->
                    Text(
                        text = bar.label,
                        style = AppTypography.Caption2,
                        color = AppColors.Gray500,
                        modifier = Modifier.width(barWidth + barSpacing),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 折线图数据点
 */
data class LineChartPoint(
    val label: String,
    val value: Float
)

/**
 * 折线图组件
 */
@Composable
fun LineChart(
    data: List<LineChartPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    lineColor: Color = AppColors.Blue,
    fillColor: Color = AppColors.Blue.copy(alpha = 0.1f),
    showDots: Boolean = true,
    showLabels: Boolean = true,
    showGridLines: Boolean = true,
    animationDuration: Int = 800
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "line_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 0f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 16.dp.toPx()

            val chartWidth = canvasWidth - padding * 2
            val chartHeight = canvasHeight - padding * 2

            val pointSpacing = chartWidth / (data.size - 1).coerceAtLeast(1)

            // 网格线
            if (showGridLines) {
                val gridColor = AppColors.Gray200
                for (i in 0..4) {
                    val y = padding + (chartHeight / 4) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(canvasWidth - padding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // 构建路径
            val linePath = Path()
            val fillPath = Path()

            data.forEachIndexed { index, point ->
                val x = padding + index * pointSpacing
                val normalizedValue = (point.value - minValue) / range
                val y = padding + chartHeight * (1 - normalizedValue * animatedProgress)

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, canvasHeight - padding)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                // 绘制数据点
                if (showDots) {
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // 填充区域
            fillPath.lineTo(padding + (data.size - 1) * pointSpacing, canvasHeight - padding)
            fillPath.close()

            drawPath(
                path = fillPath,
                color = fillColor
            )

            // 绘制折线
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // X轴标签
        if (showLabels && data.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { point ->
                    Text(
                        text = point.label,
                        style = AppTypography.Caption2,
                        color = AppColors.Gray500,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 趋势指示器
 */
@Composable
fun TrendIndicator(
    currentValue: Double,
    previousValue: Double,
    modifier: Modifier = Modifier
) {
    val change = if (previousValue != 0.0) {
        ((currentValue - previousValue) / previousValue * 100)
    } else 0.0

    val isPositive = change >= 0
    val color = when {
        change > 0 -> AppColors.Red  // 支出增加用红色
        change < 0 -> AppColors.Green  // 支出减少用绿色
        else -> AppColors.Gray500
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isPositive) "↑" else "↓",
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${String.format("%.1f", kotlin.math.abs(change))}%",
            style = AppTypography.Caption,
            color = color
        )
        Text(
            text = " 较上期",
            style = AppTypography.Caption,
            color = AppColors.Gray500
        )
    }
}

/**
 * 图例项
 */
@Composable
fun LegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.Gray700,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = AppTypography.Caption,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 格式化紧凑数字
 */
private fun formatCompactNumber(value: Float): String {
    return when {
        value >= 10000 -> "${(value / 10000).toInt()}万"
        value >= 1000 -> "${(value / 1000).toInt()}k"
        else -> value.toInt().toString()
    }
}
