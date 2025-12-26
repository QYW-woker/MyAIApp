package com.myaiapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myaiapp.ui.theme.*

/**
 * iOS风格卡片
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(AppDimens.CardRadius),
        color = Color.White,
        shadowElevation = AppDimens.CardElevation
    ) {
        Column(content = content)
    }
}

/**
 * 分类图标组件
 */
@Composable
fun CategoryIcon(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = AppDimens.CategoryIconSize
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 0.85f),
                        color
                    )
                ),
                shape = RoundedCornerShape(AppDimens.CategoryIconRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconMapper.getIcon(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * 顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.Title3
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * 大标题顶部栏 (iOS风格)
 */
@Composable
fun LargeTitleTopBar(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceMD)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = AppTypography.LargeTitle,
                    color = AppColors.Gray900
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(AppDimens.SpaceXS))
                    Text(
                        text = subtitle,
                        style = AppTypography.Subhead,
                        color = AppColors.Gray500
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpaceSM),
                content = actions
            )
        }
    }
}

/**
 * 分段控制器 (iOS风格)
 */
@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.RadiusSM),
        color = AppColors.Gray100
    ) {
        Row(
            modifier = Modifier.padding(3.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    label = "segmentBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) AppColors.Gray900 else AppColors.Gray500,
                    label = "segmentText"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(index) },
                    shape = RoundedCornerShape(AppDimens.RadiusXS),
                    color = backgroundColor,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = AppTypography.Subhead,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 主按钮
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .fillMaxWidth(),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Blue,
            disabledContainerColor = AppColors.Gray300
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = AppTypography.Button
            )
        }
    }
}

/**
 * 次要按钮
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(AppColors.Gray200, AppColors.Gray200))
        )
    ) {
        Text(
            text = text,
            style = AppTypography.Button,
            color = AppColors.Gray900
        )
    }
}

/**
 * 圆形图标按钮
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.Gray100,
    iconColor: Color = AppColors.Gray900,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/**
 * 设置列表项
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceMD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Gray600,
                modifier = Modifier.size(AppDimens.IconMD)
            )

            Spacer(modifier = Modifier.width(AppDimens.SpaceMD))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.Body
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }

            trailing?.invoke() ?: Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
                tint = AppColors.Gray400,
                modifier = Modifier
                    .size(AppDimens.IconSM)
                    .padding(start = AppDimens.SpaceSM)
            )
        }
    }
}

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.Space3XL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Gray300,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(AppDimens.SpaceLG))

        Text(
            text = title,
            style = AppTypography.Title3,
            color = AppColors.Gray600,
            textAlign = TextAlign.Center
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
            Text(
                text = subtitle,
                style = AppTypography.Body,
                color = AppColors.Gray400,
                textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = AppTypography.Button,
                    color = AppColors.Blue
                )
            }
        }
    }
}

/**
 * 进度条
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    colors: List<Color> = listOf(AppColors.Blue, AppColors.Purple),
    backgroundColor: Color = AppColors.Gray100
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(colors),
                    shape = RoundedCornerShape(height)
                )
        )
    }
}

/**
 * 标签
 */
@Composable
fun Tag(
    text: String,
    color: Color = AppColors.Blue,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.RadiusFull),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = AppTypography.Caption,
            color = color
        )
    }
}

/**
 * 金额输入显示
 */
@Composable
fun AmountDisplay(
    amount: String,
    currencySymbol: String = "¥",
    modifier: Modifier = Modifier,
    color: Color = AppColors.Gray900
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = currencySymbol,
            style = AppTypography.Title2,
            color = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (amount.isEmpty()) "0" else amount,
            style = AppTypography.AmountLarge,
            color = color
        )
    }
}
