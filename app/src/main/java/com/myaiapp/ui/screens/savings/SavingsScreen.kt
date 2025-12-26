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
    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf<SavingsPlanData?>(null) }
    var showDepositSheet by remember { mutableStateOf<SavingsPlanData?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "存钱计划",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加计划")
                    }
                }
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
                    plansCount = uiState.activePlansCount,
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
                        subtitle = "点击右上角添加您的第一个存钱计划",
                        actionText = "添加计划",
                        onAction = { showAddSheet = true }
                    )
                }
            } else {
                items(uiState.plans) { plan ->
                    SavingsCard(
                        plan = plan,
                        onClick = { showDepositSheet = plan },
                        modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // 添加计划弹窗
    if (showAddSheet) {
        SavingsEditSheet(
            onDismiss = { showAddSheet = false },
            onSave = { plan ->
                viewModel.savePlan(plan)
                showAddSheet = false
            }
        )
    }

    // 编辑计划弹窗
    showEditSheet?.let { planData ->
        SavingsEditSheet(
            plan = planData.plan,
            onDismiss = { showEditSheet = null },
            onSave = { plan ->
                viewModel.savePlan(plan)
                showEditSheet = null
            },
            onDelete = { plan ->
                viewModel.deletePlan(plan)
                showEditSheet = null
            }
        )
    }

    // 存取弹窗
    showDepositSheet?.let { planData ->
        DepositSheet(
            plan = planData.plan,
            records = planData.records,
            onDismiss = { showDepositSheet = null },
            onDeposit = { amount, note ->
                viewModel.deposit(planData.id, amount, note)
                showDepositSheet = null
            },
            onWithdraw = { amount, note ->
                viewModel.withdraw(planData.id, amount, note)
                showDepositSheet = null
            },
            onEditPlan = {
                showDepositSheet = null
                showEditSheet = planData
            }
        )
    }
}

