package com.myaiapp.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.myaiapp.data.voice.VoiceRecognitionManager
import com.myaiapp.data.voice.VoiceRecognitionState
import com.myaiapp.ui.theme.*

/**
 * 语音输入按钮
 */
@Composable
fun VoiceInputButton(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            showDialog = true
        }
    }

    IconButton(
        onClick = {
            if (hasPermission) {
                showDialog = true
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = "语音输入",
            tint = AppColors.Blue
        )
    }

    if (showDialog) {
        VoiceInputDialog(
            onDismiss = { showDialog = false },
            onResult = { text ->
                showDialog = false
                onResult(text)
            }
        )
    }
}

/**
 * 语音输入对话框
 */
@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceRecognitionManager(context) }
    val state by voiceManager.state.collectAsState()

    // 启动时开始监听
    LaunchedEffect(Unit) {
        voiceManager.startListening()
    }

    // 处理结果
    LaunchedEffect(state) {
        if (state is VoiceRecognitionState.Result) {
            val text = (state as VoiceRecognitionState.Result).text
            onResult(text)
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.release()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 关闭按钮
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "关闭",
                            tint = AppColors.Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 动画麦克风图标
                VoiceAnimationIcon(
                    isListening = state is VoiceRecognitionState.Listening,
                    isProcessing = state is VoiceRecognitionState.Processing
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 状态文本
                Text(
                    text = when (state) {
                        is VoiceRecognitionState.Idle -> "准备中..."
                        is VoiceRecognitionState.Listening -> "请说出您的记账内容"
                        is VoiceRecognitionState.Processing -> "正在识别..."
                        is VoiceRecognitionState.Result -> "识别完成"
                        is VoiceRecognitionState.Error -> (state as VoiceRecognitionState.Error).message
                    },
                    style = AppTypography.Body,
                    color = when (state) {
                        is VoiceRecognitionState.Error -> AppColors.Red
                        else -> AppColors.Gray700
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 提示文本
                Text(
                    text = "例如：\"午餐花了30元\" 或 \"打车15块\"",
                    style = AppTypography.Caption,
                    color = AppColors.Gray400
                )

                // 错误时显示重试按钮
                if (state is VoiceRecognitionState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { voiceManager.startListening() }
                    ) {
                        Text("重试", color = AppColors.Blue)
                    }
                }
            }
        }
    }
}

/**
 * 语音动画图标
 */
@Composable
private fun VoiceAnimationIcon(
    isListening: Boolean,
    isProcessing: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice")

    // 脉冲动画
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // 外圈透明度动画
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        // 外圈动画
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(
                        color = AppColors.Blue.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }

        // 主圆圈
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = when {
                        isListening -> AppColors.Blue
                        isProcessing -> AppColors.Orange
                        else -> AppColors.Gray300
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
