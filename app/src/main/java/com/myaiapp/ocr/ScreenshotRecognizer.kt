package com.myaiapp.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 截图OCR识别器
 * 使用 ML Kit 识别付款截图中的文字
 */
class ScreenshotRecognizer(private val context: Context) {

    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    /**
     * 从Uri识别文字
     */
    suspend fun recognizeFromUri(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                recognizeFromBitmap(bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从Bitmap识别文字
     */
    suspend fun recognizeFromBitmap(bitmap: Bitmap): String? {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    continuation.resume(fullText)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    continuation.resume(null)
                }
        }
    }

    /**
     * 解析OCR文本，提取关键信息
     * 这是一个本地解析的备选方案，当AI不可用时使用
     */
    fun parsePaymentText(ocrText: String): ParsedPayment? {
        try {
            var amount: Double? = null
            var payee: String? = null
            var paymentMethod: String? = null

            // 金额正则 - 支持多种格式
            val amountPatterns = listOf(
                """-?¥\s*(\d+\.?\d*)""".toRegex(),
                """金额[：:]\s*(\d+\.?\d*)""".toRegex(),
                """支付金额[：:]\s*(\d+\.?\d*)""".toRegex(),
                """实付[：:]\s*(\d+\.?\d*)""".toRegex(),
                """付款金额[：:]\s*(\d+\.?\d*)""".toRegex(),
                """(\d+\.\d{2})\s*元""".toRegex()
            )

            for (pattern in amountPatterns) {
                pattern.find(ocrText)?.let { match ->
                    amount = match.groupValues[1].toDoubleOrNull()
                    if (amount != null) return@let
                }
            }

            // 商户名称
            val payeePatterns = listOf(
                """(?:付款给|收款方|商户名称)[：:]\s*(.+)""".toRegex(),
                """(?:商户|商家)[：:]\s*(.+)""".toRegex()
            )

            for (pattern in payeePatterns) {
                pattern.find(ocrText)?.let { match ->
                    payee = match.groupValues[1].trim().take(20)
                    return@let
                }
            }

            // 支付方式判断
            paymentMethod = when {
                ocrText.contains("微信") -> "微信支付"
                ocrText.contains("支付宝") -> "支付宝"
                ocrText.contains("银行") || ocrText.contains("银联") -> "银行卡"
                ocrText.contains("云闪付") -> "云闪付"
                else -> null
            }

            return if (amount != null) {
                ParsedPayment(
                    amount = amount!!,
                    payee = payee,
                    paymentMethod = paymentMethod
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 判断是否可能是付款截图
     */
    fun isPaymentScreenshot(ocrText: String): Boolean {
        val keywords = listOf(
            "付款", "支付", "转账", "收款",
            "微信支付", "支付宝", "银行",
            "订单", "金额", "元", "¥"
        )
        return keywords.any { ocrText.contains(it) }
    }

    fun close() {
        recognizer.close()
    }
}

data class ParsedPayment(
    val amount: Double,
    val payee: String?,
    val paymentMethod: String?,
    val date: String? = null,
    val category: String? = null
)
