package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.util.*

/**
 * 分类管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storageManager = remember { FileStorageManager(context) }

    var categories by remember { mutableStateOf(storageManager.getCategories()) }
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    val incomeCategories = categories.filter { it.type == TransactionType.INCOME }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "分类管理",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "添加分类",
                            tint = AppColors.Blue
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // Tab切换
            SegmentedControl(
                items = listOf("支出", "收入"),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.SpaceLG)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppDimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceSM)
            ) {
                val displayCategories = if (selectedTab == 0) expenseCategories else incomeCategories

                items(displayCategories) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = { showEditDialog = category },
                        onDelete = { showDeleteDialog = category }
                    )
                }

                if (displayCategories.isEmpty()) {
                    item {
                        AppCard {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Category,
                                        contentDescription = null,
                                        tint = AppColors.Gray300,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "暂无分类",
                                        style = AppTypography.Body,
                                        color = AppColors.Gray400
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { showAddDialog = true }) {
                                        Text("添加分类", color = AppColors.Blue)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "提示：长按可拖动排序分类（开发中）。删除分类不会影响已有记录。",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // 添加分类对话框
    if (showAddDialog) {
        CategoryEditDialog(
            category = null,
            defaultType = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME,
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color, type ->
                val maxOrder = categories.filter { it.type == type }.maxOfOrNull { it.order } ?: 0
                val newCategory = Category(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    order = maxOrder + 1
                )
                storageManager.saveCategory(newCategory)
                categories = storageManager.getCategories()
                showAddDialog = false
            }
        )
    }

    // 编辑分类对话框
    showEditDialog?.let { category ->
        CategoryEditDialog(
            category = category,
            defaultType = category.type,
            onDismiss = { showEditDialog = null },
            onSave = { name, icon, color, type ->
                val updatedCategory = category.copy(name = name, icon = icon, color = color, type = type)
                storageManager.saveCategory(updatedCategory)
                categories = storageManager.getCategories()
                showEditDialog = null
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除分类") },
            text = {
                Text("确定要删除分类「${category.name}」吗？\n\n删除后，已使用该分类的记录将保持不变。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storageManager.deleteCategory(category.id)
                        categories = storageManager.getCategories()
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消", color = AppColors.Gray500)
                }
            }
        )
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.CardPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = category.icon,
                color = parseColor(category.color),
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = category.name,
                style = AppTypography.Body,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "编辑",
                    tint = AppColors.Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditDialog(
    category: Category?,
    defaultType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (String, String, String, TransactionType) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "shopping_cart") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#3B82F6") }
    var selectedType by remember { mutableStateOf(category?.type ?: defaultType) }

    val expenseIcons = listOf(
        "restaurant", "shopping_cart", "directions_car", "movie", "home",
        "local_hospital", "school", "phone", "spa", "fitness_center",
        "people", "flight", "pets", "card_giftcard", "more_horiz"
    )

    val incomeIcons = listOf(
        "work", "emoji_events", "business_center", "trending_up", "account_balance",
        "redeem", "replay", "more_horiz"
    )

    val icons = if (selectedType == TransactionType.EXPENSE) expenseIcons else incomeIcons

    val colors = listOf(
        "#EF4444", "#F97316", "#F59E0B", "#84CC16", "#10B981",
        "#06B6D4", "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "新建分类" else "编辑分类") },
        text = {
            Column {
                // 类型选择
                if (category == null) {
                    SegmentedControl(
                        items = listOf("支出", "收入"),
                        selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                        onItemSelected = {
                            selectedType = if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "选择图标",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .then(
                                    if (icon == selectedIcon) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = parseColor(selectedColor),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else Modifier
                                )
                                .clickable { selectedIcon = icon }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CategoryIcon(
                                icon = icon,
                                color = parseColor(selectedColor),
                                size = 36.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "选择颜色",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = AppColors.Gray900,
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                )
                                .padding(4.dp)
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = parseColor(color)
                            ) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 预览
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "预览：",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryIcon(
                        icon = selectedIcon,
                        color = parseColor(selectedColor),
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name.ifBlank { "分类名称" },
                        style = AppTypography.Body,
                        color = if (name.isBlank()) AppColors.Gray400 else AppColors.Gray900
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedIcon, selectedColor, selectedType) },
                enabled = name.isNotBlank()
            ) {
                Text("保存", color = if (name.isNotBlank()) AppColors.Blue else AppColors.Gray400)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.Gray500)
            }
        }
    )
}
