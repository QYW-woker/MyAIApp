package com.myaiapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.myaiapp.ui.theme.*

/**
 * 底部导航栏
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White.copy(alpha = 0.98f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.BottomNavHeight)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                // 首页
                NavItem(
                    icon = Icons.Outlined.Home,
                    label = "首页",
                    selected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )

                // 明细
                NavItem(
                    icon = Icons.Outlined.Receipt,
                    label = "明细",
                    selected = currentRoute == "records",
                    onClick = { onNavigate("records") }
                )

                // 中间记账按钮
                CenterAddButton(onClick = onAddClick)

                // 统计
                NavItem(
                    icon = Icons.Outlined.PieChart,
                    label = "统计",
                    selected = currentRoute == "statistics",
                    onClick = { onNavigate("statistics") }
                )

                // 资产
                NavItem(
                    icon = Icons.Outlined.CreditCard,
                    label = "资产",
                    selected = currentRoute == "assets",
                    onClick = { onNavigate("assets") }
                )
            }
            // 底部安全区域
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) AppColors.Blue else AppColors.Gray400,
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) AppColors.Blue else AppColors.Gray500,
        label = "navTextColor"
    )

    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = AppTypography.TabLabel,
            color = textColor
        )
    }
}

@Composable
private fun CenterAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .offset(y = (-AppDimens.BottomNavAddButtonOffset))
    ) {
        Surface(
            modifier = Modifier
                .size(AppDimens.BottomNavAddButtonSize)
                .align(Alignment.Center)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AppColors.Blue, AppColors.Purple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记账",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
