package com.myaiapp.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import com.myaiapp.util.formatAmount
import java.text.SimpleDateFormat
import java.util.*

/**
 * 搜索页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "返回"
                        )
                    }

                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.search(it) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("搜索交易记录、备注...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = AppColors.Gray400
                            )
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "清除",
                                        tint = AppColors.Gray400
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Blue,
                            unfocusedContainerColor = AppColors.Gray50,
                            focusedContainerColor = AppColors.Gray50
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 筛选条件
            if (uiState.query.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.filterType == null,
                        onClick = { viewModel.setFilterType(null) },
                        label = { Text("全部") }
                    )
                    FilterChip(
                        selected = uiState.filterType == "expense",
                        onClick = { viewModel.setFilterType("expense") },
                        label = { Text("支出") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Red.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Red
                        )
                    )
                    FilterChip(
                        selected = uiState.filterType == "income",
                        onClick = { viewModel.setFilterType("income") },
                        label = { Text("收入") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Green.copy(alpha = 0.1f),
                            selectedLabelColor = AppColors.Green
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 搜索结果
                if (uiState.query.isEmpty()) {
                    // 搜索历史
                    if (uiState.searchHistory.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppDimens.SpaceLG, vertical = AppDimens.SpaceSM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "搜索历史",
                                    style = AppTypography.Subhead,
                                    color = AppColors.Gray500
                                )
                                TextButton(onClick = { viewModel.clearHistory() }) {
                                    Text("清除", color = AppColors.Gray500)
                                }
                            }
                        }

                        items(uiState.searchHistory) { history ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.search(history) }
                                    .padding(horizontal = AppDimens.SpaceLG, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = AppColors.Gray400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = history,
                                    style = AppTypography.Body,
                                    color = AppColors.Gray700
                                )
                            }
                        }
                    }

                    // 热门搜索
                    item {
                        Text(
                            text = "热门搜索",
                            style = AppTypography.Subhead,
                            color = AppColors.Gray500,
                            modifier = Modifier.padding(
                                horizontal = AppDimens.SpaceLG,
                                vertical = AppDimens.SpaceSM
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.SpaceLG),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("餐饮", "交通", "购物", "娱乐").forEach { tag ->
                                SuggestionChip(
                                    onClick = { viewModel.search(tag) },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                } else {
                    // 搜索结果统计
                    item {
                        if (!uiState.isLoading) {
                            Text(
                                text = "找到 ${uiState.results.size} 条记录",
                                style = AppTypography.Caption,
                                color = AppColors.Gray500,
                                modifier = Modifier.padding(
                                    horizontal = AppDimens.SpaceLG,
                                    vertical = AppDimens.SpaceSM
                                )
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColors.Blue)
                            }
                        }
                    } else if (uiState.results.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SearchOff,
                                        contentDescription = null,
                                        tint = AppColors.Gray300,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "未找到相关记录",
                                        style = AppTypography.Body,
                                        color = AppColors.Gray400
                                    )
                                    Text(
                                        text = "试试其他关键词",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray400
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.results) { transaction ->
                            SearchResultItem(
                                transaction = transaction,
                                query = uiState.query,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    transaction: Transaction,
    query: String,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM月dd日", Locale.getDefault()) }
    val isExpense = transaction.type == com.myaiapp.data.local.model.TransactionType.EXPENSE

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.SpaceLG, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.SpaceLG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = transaction.categoryIcon ?: "more_horiz",
                color = parseColor(transaction.categoryColor ?: "#5B8DEF"),
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName ?: "其他",
                    style = AppTypography.Body
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note!!,
                        style = AppTypography.Caption,
                        color = AppColors.Gray500,
                        maxLines = 1
                    )
                }
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = AppTypography.Caption2,
                    color = AppColors.Gray400
                )
            }

            Text(
                text = "${if (isExpense) "-" else "+"}${formatAmount(transaction.amount)}",
                style = AppTypography.AmountSmall,
                color = if (isExpense) AppColors.Red else AppColors.Green
            )
        }
    }
}
