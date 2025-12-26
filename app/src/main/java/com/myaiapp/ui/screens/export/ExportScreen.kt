package com.myaiapp.ui.screens.export

import android.content.Intent
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myaiapp.data.export.ExportDateRange
import com.myaiapp.data.export.ExportFormat
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据导出页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = viewModel(factory = ExportViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }

    // 处理导出成功后的分享
    LaunchedEffect(uiState.exportedUri) {
        uiState.exportedUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when (uiState.selectedFormat) {
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.EXCEL -> "text/csv"
                    ExportFormat.PDF -> "application/pdf"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "分享导出文件"))
            viewModel.clearExportResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导出") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
            // 导出格式选择
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding)
                    ) {
                        Text(
                            text = "导出格式",
                            style = AppTypography.Title3,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExportFormatCard(
                                title = "CSV",
                                description = "通用表格格式",
                                icon = Icons.Outlined.TableChart,
                                isSelected = uiState.selectedFormat == ExportFormat.CSV,
                                onClick = { viewModel.setFormat(ExportFormat.CSV) },
                                modifier = Modifier.weight(1f)
                            )

                            ExportFormatCard(
                                title = "Excel",
                                description = "电子表格",
                                icon = Icons.Outlined.GridOn,
                                isSelected = uiState.selectedFormat == ExportFormat.EXCEL,
                                onClick = { viewModel.setFormat(ExportFormat.EXCEL) },
                                modifier = Modifier.weight(1f)
                            )

                            ExportFormatCard(
                                title = "PDF",
                                description = "报告文档",
                                icon = Icons.Outlined.PictureAsPdf,
                                isSelected = uiState.selectedFormat == ExportFormat.PDF,
                                onClick = { viewModel.setFormat(ExportFormat.PDF) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 时间范围选择
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding)
                    ) {
                        Text(
                            text = "时间范围",
                            style = AppTypography.Title3,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 预设选项
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateRangeChip(
                                text = "本月",
                                isSelected = uiState.selectedDateRange == ExportDateRange.THIS_MONTH,
                                onClick = { viewModel.setDateRange(ExportDateRange.THIS_MONTH) }
                            )
                            DateRangeChip(
                                text = "上月",
                                isSelected = uiState.selectedDateRange == ExportDateRange.LAST_MONTH,
                                onClick = { viewModel.setDateRange(ExportDateRange.LAST_MONTH) }
                            )
                            DateRangeChip(
                                text = "今年",
                                isSelected = uiState.selectedDateRange == ExportDateRange.THIS_YEAR,
                                onClick = { viewModel.setDateRange(ExportDateRange.THIS_YEAR) }
                            )
                            DateRangeChip(
                                text = "全部",
                                isSelected = uiState.selectedDateRange == ExportDateRange.ALL,
                                onClick = { viewModel.setDateRange(ExportDateRange.ALL) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 自定义日期
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (uiState.selectedDateRange == ExportDateRange.CUSTOM)
                                    AppColors.Blue.copy(alpha = 0.05f)
                                else Color.Transparent
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (uiState.selectedDateRange == ExportDateRange.CUSTOM)
                                    androidx.compose.ui.graphics.SolidColor(AppColors.Blue)
                                else androidx.compose.ui.graphics.SolidColor(AppColors.Gray200)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setDateRange(ExportDateRange.CUSTOM) }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "自定义时间段",
                                        style = AppTypography.Body,
                                        fontWeight = if (uiState.selectedDateRange == ExportDateRange.CUSTOM)
                                            FontWeight.Medium else FontWeight.Normal
                                    )

                                    if (uiState.selectedDateRange == ExportDateRange.CUSTOM) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = AppColors.Blue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (uiState.selectedDateRange == ExportDateRange.CUSTOM) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // 开始日期
                                        OutlinedButton(
                                            onClick = {
                                                isSelectingStartDate = true
                                                showDatePicker = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = uiState.customStartDate?.let {
                                                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                                        .format(Date(it))
                                                } ?: "开始日期",
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = "至",
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            color = AppColors.Gray500
                                        )

                                        // 结束日期
                                        OutlinedButton(
                                            onClick = {
                                                isSelectingStartDate = false
                                                showDatePicker = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = uiState.customEndDate?.let {
                                                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                                        .format(Date(it))
                                                } ?: "结束日期",
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 数据预览
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.CardPadding)
                    ) {
                        Text(
                            text = "数据预览",
                            style = AppTypography.Title3,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.isLoadingPreview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColors.Blue)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PreviewStatItem(
                                    label = "记录数",
                                    value = "${uiState.transactionCount}条"
                                )
                                PreviewStatItem(
                                    label = "总收入",
                                    value = "¥${String.format("%.2f", uiState.totalIncome)}",
                                    color = AppColors.Green
                                )
                                PreviewStatItem(
                                    label = "总支出",
                                    value = "¥${String.format("%.2f", uiState.totalExpense)}",
                                    color = AppColors.Red
                                )
                            }
                        }
                    }
                }
            }

            // 导出按钮
            item {
                Button(
                    onClick = { viewModel.export() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isExporting && uiState.transactionCount > 0,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Blue
                    )
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("正在导出...")
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "导出并分享",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 错误提示
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Error,
                                contentDescription = null,
                                tint = AppColors.Red
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = AppColors.Red,
                                style = AppTypography.Body
                            )
                        }
                    }
                }
            }

            // 历史导出文件
            if (uiState.exportedFiles.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "历史导出",
                            style = AppTypography.Title3,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.clearAllExports() }) {
                            Text("清空", color = AppColors.Gray500)
                        }
                    }
                }

                items(uiState.exportedFiles) { file ->
                    ExportedFileItem(
                        file = file,
                        onShare = { viewModel.shareFile(file) },
                        onDelete = { viewModel.deleteExportFile(file) }
                    )
                }
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (isSelectingStartDate)
                uiState.customStartDate ?: System.currentTimeMillis()
            else
                uiState.customEndDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            if (isSelectingStartDate) {
                                viewModel.setCustomStartDate(date)
                            } else {
                                viewModel.setCustomEndDate(date)
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ExportFormatCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Blue.copy(alpha = 0.1f) else AppColors.Gray50
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(AppColors.Blue)
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AppColors.Blue else AppColors.Gray500,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = AppTypography.Body,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AppColors.Blue else AppColors.Gray700
            )
            Text(
                text = description,
                style = AppTypography.Caption2,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun DateRangeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.Blue.copy(alpha = 0.1f),
            selectedLabelColor = AppColors.Blue
        )
    )
}

@Composable
private fun PreviewStatItem(
    label: String,
    value: String,
    color: Color = AppColors.Gray700
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = AppTypography.Title2,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.Gray500
        )
    }
}

@Composable
private fun ExportedFileItem(
    file: File,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    file.name.endsWith(".pdf") -> Icons.Outlined.PictureAsPdf
                    else -> Icons.Outlined.Description
                },
                contentDescription = null,
                tint = AppColors.Blue,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = AppTypography.Body,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = dateFormat.format(Date(file.lastModified())),
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "分享",
                    tint = AppColors.Blue
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Red
                )
            }
        }
    }
}
