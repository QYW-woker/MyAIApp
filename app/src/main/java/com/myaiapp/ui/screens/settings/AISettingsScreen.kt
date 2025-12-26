package com.myaiapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.myaiapp.data.local.model.AIProvider
import com.myaiapp.data.remote.AIRepository
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

    var config by remember { mutableStateOf(storageManager.getAppConfig()) }
    var apiKey by remember { mutableStateOf(config.aiApiKey) }
    var selectedProvider by remember { mutableStateOf(config.aiProvider) }
    var showApiKey by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var showTestDialog by remember { mutableStateOf(false) }

    val aiRepository = remember { AIRepository(context) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "AI设置",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceMD)
        ) {
            // AI功能说明
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
                            text = "AI智能分类",
                            style = AppTypography.Title3
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "启用AI后，记账时会自动根据备注内容智能识别消费分类，让记账更轻松。",
                        style = AppTypography.Body,
                        color = AppColors.Gray600
                    )
                }
            }

            // 启用AI开关
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
                            text = "启用AI分类",
                            style = AppTypography.Body
                        )
                        Text(
                            text = "自动识别消费类型",
                            style = AppTypography.Caption,
                            color = AppColors.Gray500
                        )
                    }
                    Switch(
                        checked = config.enableAI,
                        onCheckedChange = { enabled ->
                            config = config.copy(enableAI = enabled)
                            storageManager.saveAppConfig(config)
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = AppColors.Blue
                        )
                    )
                }
            }

            // AI提供商选择
            Text(
                text = "选择AI服务",
                style = AppTypography.Title3
            )

            // DeepSeek
            ProviderCard(
                name = "DeepSeek",
                description = "高性价比，支持中文",
                icon = Icons.Outlined.Psychology,
                color = AppColors.Blue,
                isSelected = selectedProvider == AIProvider.DEEPSEEK,
                onClick = {
                    selectedProvider = AIProvider.DEEPSEEK
                    config = config.copy(aiProvider = AIProvider.DEEPSEEK)
                    storageManager.saveAppConfig(config)
                }
            )

            // Groq
            ProviderCard(
                name = "Groq",
                description = "超快速度，免费额度",
                icon = Icons.Outlined.Bolt,
                color = AppColors.Orange,
                isSelected = selectedProvider == AIProvider.GROQ,
                onClick = {
                    selectedProvider = AIProvider.GROQ
                    config = config.copy(aiProvider = AIProvider.GROQ)
                    storageManager.saveAppConfig(config)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // API Key设置
            Text(
                text = "API配置",
                style = AppTypography.Title3
            )

            AppCard {
                Column(
                    modifier = Modifier.padding(AppDimens.CardPadding)
                ) {
                    Text(
                        text = when (selectedProvider) {
                            AIProvider.DEEPSEEK -> "DeepSeek API Key"
                            AIProvider.GROQ -> "Groq API Key"
                        },
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("请输入API Key") },
                        visualTransformation = if (showApiKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (showApiKey) "隐藏" else "显示"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Gray200,
                            focusedBorderColor = AppColors.Blue
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 获取API Key链接
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedProvider) {
                                AIProvider.DEEPSEEK -> "获取Key: platform.deepseek.com"
                                AIProvider.GROQ -> "获取Key: console.groq.com"
                            },
                            style = AppTypography.Caption,
                            color = AppColors.Blue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 保存和测试按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTesting = true
                                scope.launch {
                                    // 先保存配置
                                    config = config.copy(aiApiKey = apiKey)
                                    storageManager.saveAppConfig(config)

                                    // 测试API
                                    val result = aiRepository.testConnection()
                                    testResult = if (result) {
                                        "连接成功！AI服务可正常使用"
                                    } else {
                                        "连接失败，请检查API Key是否正确"
                                    }
                                    showTestDialog = true
                                    isTesting = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = apiKey.isNotBlank() && !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.NetworkCheck,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("测试连接")
                        }

                        Button(
                            onClick = {
                                config = config.copy(aiApiKey = apiKey)
                                storageManager.saveAppConfig(config)
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Blue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存")
                        }
                    }
                }
            }

            // 使用说明
            AppCard {
                Column(
                    modifier = Modifier.padding(AppDimens.CardPadding)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppColors.Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "使用说明",
                            style = AppTypography.Body,
                            color = AppColors.Gray600
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. 选择AI服务提供商\n2. 前往官网注册并获取API Key\n3. 填入API Key并保存\n4. 记账时输入备注即可自动识别分类",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }

            // 费用说明
            AppCard {
                Column(
                    modifier = Modifier.padding(AppDimens.CardPadding)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = null,
                            tint = AppColors.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "费用说明",
                            style = AppTypography.Body,
                            color = AppColors.Gray600
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• DeepSeek: 约0.001元/次分类\n• Groq: 提供免费额度，超出后按量计费\n\n每次记账仅消耗极少Token，日常使用几乎免费。",
                        style = AppTypography.Caption,
                        color = AppColors.Gray500
                    )
                }
            }
        }
    }

    // 测试结果对话框
    if (showTestDialog && testResult != null) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = { Text("测试结果") },
            text = { Text(testResult!!) },
            confirmButton = {
                TextButton(onClick = { showTestDialog = false }) {
                    Text("确定", color = AppColors.Blue)
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
    name: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AppCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = AppTypography.Title3
                )
                Text(
                    text = description,
                    style = AppTypography.Caption,
                    color = AppColors.Gray500
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.Blue
                )
            )
        }
    }
}
