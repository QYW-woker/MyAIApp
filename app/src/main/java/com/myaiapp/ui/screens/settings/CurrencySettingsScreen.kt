package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.local.model.Currency
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*

/**
 * 货币设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    onBack: () -> Unit,
    viewModel: CurrencySettingsViewModel = viewModel(
        factory = CurrencySettingsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddCurrencySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "货币设置",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 默认货币
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding)
                    ) {
                        Text(
                            text = "默认货币",
                            style = AppTypography.Title3,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "新记录默认使用的货币",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        uiState.currencies.take(5).forEach { currency ->
                            CurrencyRadioItem(
                                currency = currency,
                                isSelected = currency.code == uiState.defaultCurrency,
                                onClick = { viewModel.setDefaultCurrency(currency.code) }
                            )
                        }
                    }
                }
            }

            // 常用货币
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "所有货币",
                        style = AppTypography.Title3,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.updateExchangeRates() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("更新汇率")
                    }
                }
            }

            items(uiState.currencies) { currency ->
                CurrencyListItem(
                    currency = currency,
                    baseCurrency = uiState.defaultCurrency,
                    isDefault = currency.code == uiState.defaultCurrency
                )
            }

            // 汇率说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Blue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppColors.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "汇率说明",
                                style = AppTypography.Body,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Blue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "汇率仅供参考，实际汇率以银行为准。\n统计时会自动将其他货币转换为默认货币。",
                                style = AppTypography.Caption,
                                color = AppColors.Gray600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRadioItem(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppColors.Blue
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = currency.symbol,
            style = AppTypography.Title2,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.name,
                style = AppTypography.Body,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currency.code,
                style = AppTypography.Caption,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun CurrencyListItem(
    currency: Currency,
    baseCurrency: String,
    isDefault: Boolean
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 货币符号
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDefault) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = currency.symbol,
                        style = AppTypography.Title2,
                        color = if (isDefault) AppColors.Blue else AppColors.Gray700
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 货币信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.name,
                        style = AppTypography.Body,
                        fontWeight = FontWeight.Medium
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.Blue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "默认",
                                style = AppTypography.Caption2,
                                color = AppColors.Blue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = currency.code,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            // 汇率
            if (!isDefault) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1 $baseCurrency =",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Text(
                        text = "${String.format("%.4f", 1 / currency.rate)} ${currency.code}",
                        style = AppTypography.Body,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
