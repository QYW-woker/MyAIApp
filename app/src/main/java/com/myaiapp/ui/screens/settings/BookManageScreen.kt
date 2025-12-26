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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Book
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import java.util.*

/**
 * 账本管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookManageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storageManager = remember { FileStorageManager(context) }

    var books by remember { mutableStateOf(storageManager.getBooks()) }
    var currentBookId by remember { mutableStateOf(storageManager.getCurrentBookId()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Book?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "账本管理",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "添加账本",
                            tint = AppColors.Blue
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .padding(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceMD)
        ) {
            item {
                Text(
                    text = "我的账本",
                    style = AppTypography.Title3,
                    color = AppColors.Gray600
                )
            }

            items(books) { book ->
                BookItem(
                    book = book,
                    isCurrent = book.id == currentBookId,
                    onSelect = {
                        currentBookId = book.id
                        storageManager.setCurrentBookId(book.id)
                    },
                    onEdit = { showEditDialog = book },
                    onDelete = { showDeleteDialog = book }
                )
            }

            if (books.isEmpty()) {
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
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = AppColors.Gray300,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "暂无账本",
                                    style = AppTypography.Body,
                                    color = AppColors.Gray400
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showAddDialog = true }) {
                                    Text("创建账本", color = AppColors.Blue)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "提示：可以创建多个账本分别记录不同用途的收支，如日常、旅行、项目等。",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }
        }
    }

    // 添加账本对话框
    if (showAddDialog) {
        BookEditDialog(
            book = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color ->
                val newBook = Book(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    icon = icon,
                    color = color,
                    createdAt = System.currentTimeMillis()
                )
                storageManager.saveBook(newBook)
                books = storageManager.getBooks()
                if (books.size == 1) {
                    currentBookId = newBook.id
                    storageManager.setCurrentBookId(newBook.id)
                }
                showAddDialog = false
            }
        )
    }

    // 编辑账本对话框
    showEditDialog?.let { book ->
        BookEditDialog(
            book = book,
            onDismiss = { showEditDialog = null },
            onSave = { name, icon, color ->
                val updatedBook = book.copy(name = name, icon = icon, color = color)
                storageManager.saveBook(updatedBook)
                books = storageManager.getBooks()
                showEditDialog = null
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { book ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除账本") },
            text = {
                Text("确定要删除账本「${book.name}」吗？\n\n该账本下的所有记录也将被删除，此操作不可恢复！")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storageManager.deleteBook(book.id)
                        books = storageManager.getBooks()
                        if (currentBookId == book.id && books.isNotEmpty()) {
                            currentBookId = books.first().id
                            storageManager.setCurrentBookId(books.first().id)
                        }
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
private fun BookItem(
    book: Book,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard(
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = book.icon,
                color = parseColor(book.color),
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.name,
                        style = AppTypography.Title3
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.Blue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "当前",
                                style = AppTypography.Caption2,
                                color = AppColors.Blue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "创建于 ${formatDate(book.createdAt)}",
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "编辑",
                    tint = AppColors.Gray400
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Gray400
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookEditDialog(
    book: Book?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(book?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(book?.icon ?: "book") }
    var selectedColor by remember { mutableStateOf(book?.color ?: "#3B82F6") }

    val icons = listOf(
        "book", "wallet", "credit_card", "shopping_bag", "airplane",
        "home", "briefcase", "gift", "heart", "star"
    )

    val colors = listOf(
        "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6",
        "#EC4899", "#06B6D4", "#84CC16", "#F97316", "#6366F1"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (book == null) "新建账本" else "编辑账本") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账本名称") },
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedIcon, selectedColor) },
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

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
