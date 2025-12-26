package com.myaiapp.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatDate
import com.myaiapp.util.formatDayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onAddRecord: () -> Unit,
    viewModel: RecordsViewModel = viewModel(factory = RecordsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "账单明细",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { /* TODO: Filter */ }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "筛选")
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecord,
                containerColor = AppColors.Blue
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "记账",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 筛选标签
            SegmentedControl(
                items = listOf("全部", "支出", "收入"),
                selectedIndex = uiState.filterIndex,
                onItemSelected = { viewModel.setFilter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
            )

            // 月度汇总
            SummaryCard(
                income = uiState.periodIncome,
                expense = uiState.periodExpense,
                balance = uiState.periodBalance,
                modifier = Modifier.padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM)
            )

            // 交易列表
            if (uiState.transactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = "暂无记录",
                    subtitle = "点击右下角按钮开始记账"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val groupedTransactions = uiState.transactions.groupBy { formatDate(it.date) }

                    groupedTransactions.forEach { (date, transactions) ->
                        val dayIncome = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val dayExpense = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        item {
                            DateHeader(
                                date = date,
                                dayOfWeek = formatDayOfWeek(transactions.first().date),
                                income = dayIncome,
                                expense = dayExpense
                            )
                        }

                        items(transactions) { transaction ->
                            val category = uiState.categories.find { it.id == transaction.categoryId }
                            if (category != null) {
                                Surface(color = Color.White) {
                                    TransactionItem(
                                        transaction = transaction,
                                        category = category,
                                        onClick = { onTransactionClick(transaction.id) }
                                    )
                                }
                                Divider(
                                    modifier = Modifier.padding(start = 74.dp),
                                    color = AppColors.Gray100
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
