package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.AIConfig
import com.myaiapp.ui.components.*
import com.myaiapp.ui.theme.*
import kotlinx.coroutines.launch

/**
 * AI设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageManager = remember { FileStorageManager(context) }

    var config by remember { mutableStateOf(AIConfig()) }
    var isLoading by remember { mutableStateOf(true) }
    var showApiKey by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }

    // 加载配置
    LaunchedEffect(Unit) {
        config = storageManager.getAIConfig()
        isLoading = false
    }

    // 保存配置
    fun saveConfig() {
        scope.launch {
            storageManager.saveAIConfig(config)
            message = "设置已保存"
            showMessageDialog = true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "AI智能助手",
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
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = AppColors.Purple,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "AI智能功能",
                                    style = AppTypography.Title3
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "配置AI后可启用智能分类、截图识别等功能。支持DeepSeek和Groq API。",
                                style = AppTypography.Caption,
                                color = AppColors.Gray500
                            )
                        }
                    }
                }

                // API提供商选择
                item {
                    AppCard {
                        Column(
                            modifier = Modifier.padding(AppDimens.CardPadding)
                        ) {
                            Text(
                                text = "API提供商",
                                style = AppTypography.Subhead,
                                color = AppColors.Gray600
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProviderButton(
                                    name = "DeepSeek",
                                    isSelected = config.provider == "deepseek",
                                    onClick = {
                                        config = config.copy(
                                            provider = "deepseek",
                                            baseUrl = "https://api.deepseek.com",
                                            model = "deepseek-chat"
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                ProviderButton(
                                    name = "Groq",
                                    isSelected = config.provider == "groq",
                                    onClick = {
                                        config = config.copy(
                                            provider = "groq",
                                            baseUrl = "https://api.groq.com/openai",
                                            model = "llama-3.1-70b-versatile"
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // API配置
                item {
                    AppCard {
                        Column(
                            modifier = Modifier.padding(AppDimens.CardPadding)
                        ) {
                            Text(
                                text = "API配置",
                                style = AppTypography.Subhead,
                                color = AppColors.Gray600
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // API Key
                            OutlinedTextField(
                                value = config.apiKey,
                                onValueChange = { config = config.copy(apiKey = it) },
                                label = { Text("API Key") },
                                placeholder = { Text("输入你的API Key") },
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = if (showApiKey) "隐藏" else "显示"
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Base URL
                            OutlinedTextField(
                                value = config.baseUrl,
                                onValueChange = { config = config.copy(baseUrl = it) },
                                label = { Text("Base URL") },
                                placeholder = { Text("API基础地址") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Blue,
                                    unfocusedBorderColor = AppColors.Gray200
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Model
                            OutlinedTextField(
                                value = config.model,
                                onValueChange = { config = config.copy(model = it) },
                                label = { Text("模型") },
                                placeholder = { Text("模型名称") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Blue,
                                    unfocusedBorderColor = AppColors.Gray200
                                )
                            )
                        }
                    }
                }

                // 功能开关
                item {
                    AppCard {
                        Column(
                            modifier = Modifier.padding(AppDimens.CardPadding)
                        ) {
                            Text(
                                text = "功能设置",
                                style = AppTypography.Subhead,
                                color = AppColors.Gray600
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "智能分类",
                                        style = AppTypography.Body
                                    )
                                    Text(
                                        text = "自动识别消费分类",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }
                                Switch(
                                    checked = config.enableAutoClassify,
                                    onCheckedChange = { config = config.copy(enableAutoClassify = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AppColors.Blue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = AppColors.Gray100)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "截图识别",
                                        style = AppTypography.Body
                                    )
                                    Text(
                                        text = "从支付截图提取信息",
                                        style = AppTypography.Caption,
                                        color = AppColors.Gray500
                                    )
                                }
                                Switch(
                                    checked = config.enableOCR,
                                    onCheckedChange = { config = config.copy(enableOCR = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AppColors.Blue
                                    )
                                )
                            }
                        }
                    }
                }

                // 保存按钮
                item {
                    Button(
                        onClick = { saveConfig() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Blue
                        )
                    ) {
                        Text(
                            text = "保存设置",
                            style = AppTypography.Body
                        )
                    }
                }

                // 提示说明
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "提示：API Key将安全存储在本地，不会上传到任何服务器。",
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

@Composable
private fun ProviderButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AppColors.Blue else AppColors.Gray100,
            contentColor = if (isSelected) Color.White else AppColors.Gray600
        )
    ) {
        Text(name)
    }
}
