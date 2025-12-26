package com.myaiapp.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * 语音识别管理器
 */
class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    /**
     * 识别状态
     */
    sealed class RecognitionState {
        object Idle : RecognitionState()
        object Listening : RecognitionState()
        object Processing : RecognitionState()
        data class Result(val text: String) : RecognitionState()
        data class Error(val message: String) : RecognitionState()
    }

    /**
     * 检查语音识别是否可用
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * 开始语音识别
     */
    fun startListening() {
        if (isListening) return

        if (!isAvailable()) {
            _recognitionState.value = RecognitionState.Error("设备不支持语音识别")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            _recognitionState.value = RecognitionState.Listening
        } catch (e: Exception) {
            _recognitionState.value = RecognitionState.Error("启动语音识别失败: ${e.message}")
        }
    }

    /**
     * 停止语音识别
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消语音识别
     */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            isListening = false
            _recognitionState.value = RecognitionState.Idle
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 释放资源
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 创建识别监听器
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.Listening
            }

            override fun onBeginningOfSpeech() {
                // 开始说话
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 音量变化
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 接收到音频数据
            }

            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                    SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                    SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器繁忙"
                    SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有检测到语音"
                    else -> "未知错误"
                }
                _recognitionState.value = RecognitionState.Error(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _recognizedText.value = text
                _recognitionState.value = RecognitionState.Result(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotEmpty()) {
                    _recognizedText.value = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // 其他事件
            }
        }
    }

    /**
     * 重置状态
     */
    fun reset() {
        _recognitionState.value = RecognitionState.Idle
        _recognizedText.value = ""
    }
}
