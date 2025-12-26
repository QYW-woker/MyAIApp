package com.myaiapp.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.myaiapp.ui.theme.*
import com.myaiapp.voice.VoiceInputParser
import com.myaiapp.voice.VoiceRecognitionManager
import kotlinx.coroutines.delay

/**
 * 语音记账按钮
 */
@Composable
fun VoiceRecordButton(
    modifier: Modifier = Modifier,
    onResult: (VoiceInputParser.ParseResult) -> Unit,
    onError: (String) -> Unit
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
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            showDialog = true
        } else {
            onError("需要麦克风权限才能使用语音记账")
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
            contentDescription = "语音记账",
            tint = AppColors.Primary
        )
    }

    if (showDialog) {
        VoiceRecordDialog(
            onDismiss = { showDialog = false },
            onResult = { result ->
                showDialog = false
                onResult(result)
            },
            onError = { error ->
                showDialog = false
                onError(error)
            }
        )
    }
}

/**
 * 语音录制对话框
 */
@Composable
private fun VoiceRecordDialog(
    onDismiss: () -> Unit,
    onResult: (VoiceInputParser.ParseResult) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceRecognitionManager(context) }
    val voiceParser = remember { VoiceInputParser() }

    val recognitionState by voiceManager.recognitionState.collectAsState()
    val recognizedText by voiceManager.recognizedText.collectAsState()

    // 启动语音识别
    LaunchedEffect(Unit) {
        voiceManager.startListening()
    }

    // 处理识别结果
    LaunchedEffect(recognitionState) {
        when (val state = recognitionState) {
            is VoiceRecognitionManager.RecognitionState.Result -> {
                delay(500) // 短暂延迟让用户看到结果
                val result = voiceParser.parse(state.text)
                onResult(result)
            }
            is VoiceRecognitionManager.RecognitionState.Error -> {
                delay(1000)
                onError(state.message)
            }
            else -> {}
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    Dialog(
        onDismissRequest = {
            voiceManager.cancel()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
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
                    IconButton(
                        onClick = {
                            voiceManager.cancel()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "关闭",
                            tint = AppColors.Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 状态指示器
                VoiceStatusIndicator(
                    state = recognitionState,
                    onStop = { voiceManager.stopListening() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 识别文字
                Text(
                    text = when (recognitionState) {
                        is VoiceRecognitionManager.RecognitionState.Idle -> "准备中..."
                        is VoiceRecognitionManager.RecognitionState.Listening ->
                            if (recognizedText.isEmpty()) "请说出记账内容" else recognizedText
                        is VoiceRecognitionManager.RecognitionState.Processing -> recognizedText.ifEmpty { "正在识别..." }
                        is VoiceRecognitionManager.RecognitionState.Result ->
                            (recognitionState as VoiceRecognitionManager.RecognitionState.Result).text
                        is VoiceRecognitionManager.RecognitionState.Error ->
                            (recognitionState as VoiceRecognitionManager.RecognitionState.Error).message
                    },
                    style = AppTypography.Body,
                    color = when (recognitionState) {
                        is VoiceRecognitionManager.RecognitionState.Error -> AppColors.Red
                        else -> AppColors.Gray900
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 提示文字
                Text(
                    text = "例如：\"买咖啡花了25块\" 或 \"收到工资8000元\"",
                    style = AppTypography.Caption,
                    color = AppColors.Gray400,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 语音状态指示器
 */
@Composable
private fun VoiceStatusIndicator(
    state: VoiceRecognitionManager.RecognitionState,
    onStop: () -> Unit
) {
    val isListening = state is VoiceRecognitionManager.RecognitionState.Listening

    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈脉冲
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(AppColors.Primary.copy(alpha = alpha))
            )
        }

        // 内圈
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = when (state) {
                is VoiceRecognitionManager.RecognitionState.Listening -> AppColors.Primary
                is VoiceRecognitionManager.RecognitionState.Processing -> AppColors.Orange
                is VoiceRecognitionManager.RecognitionState.Error -> AppColors.Red
                else -> AppColors.Gray300
            },
            onClick = { if (isListening) onStop() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Outlined.Stop else Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = when (state) {
            is VoiceRecognitionManager.RecognitionState.Idle -> "准备中"
            is VoiceRecognitionManager.RecognitionState.Listening -> "正在聆听..."
            is VoiceRecognitionManager.RecognitionState.Processing -> "识别中..."
            is VoiceRecognitionManager.RecognitionState.Result -> "识别完成"
            is VoiceRecognitionManager.RecognitionState.Error -> "识别失败"
        },
        style = AppTypography.Caption,
        fontWeight = FontWeight.Medium,
        color = AppColors.Gray600
    )
}
