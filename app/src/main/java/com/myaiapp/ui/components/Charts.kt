package com.myaiapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myaiapp.ui.theme.AppColors
import com.myaiapp.ui.theme.AppTypography

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
    strokeWidth: Dp = 30.dp
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            var startAngle = -90f
            val sweepAngles = data.map { (it.value / total) * 360f }

            data.forEachIndexed { index, item ->
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[index],
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt),
                    size = Size(
                        size.toPx() - strokeWidth.toPx(),
                        size.toPx() - strokeWidth.toPx()
                    ),
                    topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
                )
                startAngle += sweepAngles[index]
            }
        }
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = AppTypography.Caption,
                color = AppColors.Gray700
            )
        }
        Text(
            text = value,
            style = AppTypography.Caption,
            color = AppColors.Gray500
        )
    }
}

/**
 * 柱状图数据
 */
data class BarChartData(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * 柱状图组件
 */
@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    barWidth: Dp = 24.dp
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 0.0
    if (maxValue == 0.0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val barHeight = ((item.value / maxValue) * (height.value - 20)).dp

                Canvas(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight.coerceAtLeast(4.dp))
                ) {
                    drawRoundRect(
                        color = item.color,
                        size = Size(size.width, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.label,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

/**
 * 折线图数据点
 */
data class LineChartPoint(
    val label: String,
    val value: Double
)

/**
 * 折线图组件
 */
@Composable
fun LineChart(
    data: List<LineChartPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    lineColor: Color = AppColors.Blue,
    fillColor: Color = AppColors.Blue.copy(alpha = 0.1f)
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 0.0
    if (maxValue == 0.0) return

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - 20.dp)
        ) {
            val width = size.width
            val chartHeight = size.height
            val pointCount = data.size
            if (pointCount < 2) return@Canvas

            val stepX = width / (pointCount - 1)
            val points = data.mapIndexed { index, point ->
                val x = index * stepX
                val y = chartHeight - ((point.value / maxValue) * chartHeight).toFloat()
                Offset(x, y)
            }

            // 绘制填充区域
            val fillPath = Path().apply {
                moveTo(0f, chartHeight)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(width, chartHeight)
                close()
            }
            drawPath(fillPath, fillColor, style = Fill)

            // 绘制折线
            val linePath = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        moveTo(point.x, point.y)
                    } else {
                        lineTo(point.x, point.y)
                    }
                }
            }
            drawPath(
                linePath,
                lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 绘制数据点
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }

        // 底部标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
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
    val change = if (previousValue > 0) {
        ((currentValue - previousValue) / previousValue * 100)
    } else {
        0.0
    }

    val isIncrease = change > 0
    val color = if (isIncrease) AppColors.Red else AppColors.Green
    val arrow = if (isIncrease) "↑" else "↓"
    val text = if (change == 0.0) "持平" else "$arrow ${String.format("%.1f", kotlin.math.abs(change))}%"

    Text(
        text = "较上期 $text",
        style = AppTypography.Caption,
        color = if (change == 0.0) AppColors.Gray500 else color,
        modifier = modifier
    )
}
