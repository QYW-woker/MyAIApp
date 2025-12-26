package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.SyncConfig
import com.myaiapp.data.sync.SyncManager
import com.myaiapp.data.sync.SyncMeta
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 云同步设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageManager = remember { FileStorageManager(context) }
    val syncManager = remember { SyncManager(context, storageManager) }

    var config by remember { mutableStateOf(SyncConfig()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var cloudMeta by remember { mutableStateOf<SyncMeta?>(null) }

    // 加载配置
    LaunchedEffect(Unit) {
        config = storageManager.getSyncConfig()
        isLoading = false
        // 获取云端同步信息
        if (config.enabled) {
            cloudMeta = syncManager.getCloudSyncInfo()
        }
    }

    // 测试连接
    fun testConnection() {
        scope.launch {
            isSyncing = true
            val result = syncManager.testConnection(config)
            message = if (result.isSuccess) {
                "连接成功！"
            } else {
                "连接失败: ${result.exceptionOrNull()?.message}"
            }
            showMessageDialog = true
            isSyncing = false
        }
    }

    // 保存配置
    fun saveConfig() {
        scope.launch {
            storageManager.saveSyncConfig(config)
            message = "设置已保存"
            showMessageDialog = true
        }
    }

    // 上传到云端
    fun uploadToCloud() {
        scope.launch {
            isSyncing = true
            val result = syncManager.uploadToCloud()
            message = if (result.isSuccess) {
                cloudMeta = syncManager.getCloudSyncInfo()
                "上传成功！"
            } else {
                "上传失败: ${result.exceptionOrNull()?.message}"
            }
            showMessageDialog = true
            isSyncing = false
        }
    }

    // 从云端下载
    fun downloadFromCloud() {
        scope.launch {
            isSyncing = true
            val result = syncManager.downloadFromCloud()
            message = if (result.isSuccess) {
                "下载成功！请重启应用以应用更改"
            } else {
                "下载失败: ${result.exceptionOrNull()?.message}"
            }
            showMessageDialog = true
            isSyncing = false
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "云同步",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Blue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues)
                    .padding(AppDimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceMD)
            ) {
                // 说明卡片
                item {
                    AppCard {
                        Column(
                            modifier = Modifier.padding(AppDimens.CardPadding)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Cloud,
                                    contentDescription = null,
                                    tint = AppColors.Blue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "WebDAV 云同步",
                                    style = AppTypography.Title3
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "支持坚果云、NextCloud、Alist 等 WebDAV 服务，实现多设备数据同步。",
                                style = AppTypography.Caption,
                                color = AppColors.Gray500
                            )
                        }
                    }
                }

                // 启用开关
                item {
                    AppCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDimens.CardPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "启用云同步",
                                    style = AppTypography.Body
                                )
                                Text(
                                    text = "开启后可同步数据到云端",
                                    style = AppTypography.Caption,
                                    color = AppColors.Gray500
                                )
                            }
                            Switch(
                                checked = config.enabled,
                                onCheckedChange = { config = config.copy(enabled = it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppColors.Blue
                                )
                            )
                        }
                    }
                }

                // WebDAV 配置
                if (config.enabled) {
                    item {
                        AppCard {
                            Column(
                                modifier = Modifier.padding(AppDimens.CardPadding)
                            ) {
                                Text(
                                    text = "WebDAV 配置",
                                    style = AppTypography.Subhead,
                                    color = AppColors.Gray600
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // 服务器地址
                                OutlinedTextField(
                                    value = config.serverUrl,
                                    onValueChange = { config = config.copy(serverUrl = it) },
                                    label = { Text("服务器地址") },
                                    placeholder = { Text("https://dav.jianguoyun.com/dav/") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Blue,
                                        unfocusedBorderColor = AppColors.Gray200
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 用户名
                                OutlinedTextField(
                                    value = config.username,
                                    onValueChange = { config = config.copy(username = it) },
                                    label = { Text("用户名") },
                                    placeholder = { Text("邮箱或用户名") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Blue,
                                        unfocusedBorderColor = AppColors.Gray200
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 密码
                                OutlinedTextField(
                                    value = config.password,
                                    onValueChange = { config = config.copy(password = it) },
                                    label = { Text("密码/应用密码") },
                                    placeholder = { Text("输入密码") },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                contentDescription = if (showPassword) "隐藏" else "显示"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Blue,
                                        unfocusedBorderColor = AppColors.Gray200
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 测试连接和保存按钮
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { testConnection() },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSyncing && config.serverUrl.isNotBlank()
                                    ) {
                                        Text("测试连接")
                                    }
                                    Button(
                                        onClick = { saveConfig() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Blue
                                        )
                                    ) {
                                        Text("保存设置")
                                    }
                                }
                            }
                        }
                    }

                    // 同步操作
                    item {
                        AppCard {
                            Column(
                                modifier = Modifier.padding(AppDimens.CardPadding)
                            ) {
                                Text(
                                    text = "同步操作",
                                    style = AppTypography.Subhead,
                                    color = AppColors.Gray600
                                )

                                // 显示云端同步信息
                                cloudMeta?.let { meta ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "云端最后同步: ${meta.formattedTime}",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }

                                // 显示本地同步信息
                                if (config.lastSyncTime > 0) {
                                    val localTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        .format(Date(config.lastSyncTime))
                                    Text(
                                        text = "本地最后同步: $localTime",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 同步按钮
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 上传
                                    Button(
                                        onClick = { uploadToCloud() },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSyncing && config.serverUrl.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Green
                                        )
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.CloudUpload,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("上传")
                                    }

                                    // 下载
                                    Button(
                                        onClick = { downloadFromCloud() },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSyncing && config.serverUrl.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Orange
                                        )
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.CloudDownload,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("下载")
                                    }
                                }
                            }
                        }
                    }
                }

                // 使用说明
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "提示：\n" +
                                "• 坚果云需使用「应用密码」而非登录密码\n" +
                                "• 上传会覆盖云端数据，下载会覆盖本地数据\n" +
                                "• 建议在同步前先备份数据",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }
        }
    }

    // 消息对话框
    if (showMessageDialog && message != null) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("提示") },
            text = { Text(message!!) },
            confirmButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("确定", color = AppColors.Blue)
                }
            }
        )
    }
}
