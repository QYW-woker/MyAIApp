package com.myaiapp.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    onBack: () -> Unit,
    viewModel: SavingsViewModel = viewModel(factory = SavingsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "存钱计划",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 总览卡片
            item {
                SavingsTotalCard(
                    totalSaved = uiState.totalSaved,
                    totalTarget = uiState.totalTarget,
                    plansCount = uiState.plans.size,
                    modifier = Modifier.padding(AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceXL))
            }

            // 计划标题
            item {
                Text(
                    text = "进行中的计划",
                    style = AppTypography.Title3,
                    modifier = Modifier.padding(horizontal = AppDimens.SpaceLG)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpaceSM))
            }

            if (uiState.plans.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Add,
                        title = "暂无存钱计划",
                        subtitle = "存钱计划功能即将推出"
                    )
                }
            } else {
                items(uiState.plans) { plan ->
                    SavingsCardCompact(
                        plan = plan,
                        onClick = { },
                        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
