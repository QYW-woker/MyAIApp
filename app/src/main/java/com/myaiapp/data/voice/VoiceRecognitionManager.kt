package com.myaiapp.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 语音识别状态
 */
sealed class VoiceRecognitionState {
    object Idle : VoiceRecognitionState()
    object Listening : VoiceRecognitionState()
    object Processing : VoiceRecognitionState()
    data class Result(val text: String) : VoiceRecognitionState()
    data class Error(val message: String) : VoiceRecognitionState()
}

/**
 * 语音识别管理器
 * 使用 Android 内置的语音识别功能
 */
class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    /**
     * 检查设备是否支持语音识别
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * 开始语音识别
     */
    fun startListening() {
        if (!isAvailable()) {
            _state.value = VoiceRecognitionState.Error("设备不支持语音识别")
            return
        }

        // 释放之前的实例
        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceRecognitionState.Listening
                }

                override fun onBeginningOfSpeech() {
                    // 用户开始说话
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // 音量变化，可用于显示音量动画
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // 接收到音频缓冲
                }

                override fun onEndOfSpeech() {
                    _state.value = VoiceRecognitionState.Processing
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到语音"
                        else -> "未知错误"
                    }
                    _state.value = VoiceRecognitionState.Error(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) {
                        _state.value = VoiceRecognitionState.Result(text)
                    } else {
                        _state.value = VoiceRecognitionState.Error("未识别到语音")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // 部分识别结果，可用于实时显示
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // 其他事件
                }
            })
        }

        // 创建识别意图
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
    }

    /**
     * 停止语音识别
     */
    fun stopListening() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
        _state.value = VoiceRecognitionState.Idle
    }

    /**
     * 重置状态
     */
    fun reset() {
        _state.value = VoiceRecognitionState.Idle
    }

    /**
     * 释放资源
     */
    fun release() {
        stopListening()
    }
}
