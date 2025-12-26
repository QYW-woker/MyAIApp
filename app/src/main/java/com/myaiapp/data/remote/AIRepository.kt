package com.myaiapp.data.remote

import com.google.gson.Gson
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * AI API 仓库
 */
class AIRepository(
    private val storageManager: FileStorageManager
) {
    private val gson = Gson()

    private suspend fun getApiService(): Pair<AIApiService, String>? {
        val config = storageManager.getAIConfig()
        if (config.apiKey.isBlank()) return null

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return Pair(retrofit.create(AIApiService::class.java), config.apiKey)
    }

    /**
     * 智能分类
     * 根据消费描述自动识别分类
     */
    suspend fun classifyTransaction(
        description: String,
        categories: List<Category>
    ): String? = withContext(Dispatchers.IO) {
        try {
            val (service, apiKey) = getApiService() ?: return@withContext null
            val config = storageManager.getAIConfig()

            val categoryNames = categories.map { it.name }.joinToString("、")
            val prompt = """
你是一个记账分类助手。根据以下消费描述，从给定分类中选择最合适的一个。

消费描述：$description

可选分类：$categoryNames

只返回分类名称，不要其他内容。
            """.trimIndent()

            val response = service.chatCompletion(
                authorization = "Bearer $apiKey",
                request = ChatRequest(
                    model = config.model,
                    messages = listOf(Message("user", prompt)),
                    temperature = 0.3,
                    maxTokens = 50
                )
            )

            val result = response.choices?.firstOrNull()?.message?.content?.trim()
            categories.find { it.name == result }?.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 解析截图OCR文本
     * 提取金额、商户、时间等信息
     */
    suspend fun parseScreenshotText(ocrText: String): TransactionParseResult? = withContext(Dispatchers.IO) {
        try {
            val (service, apiKey) = getApiService() ?: return@withContext null
            val config = storageManager.getAIConfig()

            val prompt = """
分析以下付款截图的OCR文本，提取记账信息。

OCR文本：
$ocrText

请以JSON格式返回（如果某项无法识别则为null）：
{
    "amount": 数字金额（不带货币符号），
    "payee": "收款方/商户名称",
    "paymentMethod": "支付方式（如微信、支付宝、银行卡）",
    "date": "日期时间字符串",
    "category": "推测的消费分类（如餐饮、购物、交通等）",
    "note": "备注信息"
}

只返回JSON，不要其他内容。
            """.trimIndent()

            val response = service.chatCompletion(
                authorization = "Bearer $apiKey",
                request = ChatRequest(
                    model = config.model,
                    messages = listOf(Message("user", prompt)),
                    temperature = 0.1,
                    maxTokens = 200
                )
            )

            val jsonStr = response.choices?.firstOrNull()?.message?.content?.trim()
            if (jsonStr != null) {
                try {
                    gson.fromJson(jsonStr, TransactionParseResult::class.java)
                } catch (e: Exception) {
                    null
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 智能生成备注
     */
    suspend fun generateNote(
        amount: Double,
        category: String,
        payee: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val (service, apiKey) = getApiService() ?: return@withContext null
            val config = storageManager.getAIConfig()

            val prompt = """
根据以下消费信息，生成一个简短的备注（不超过10个字）：
- 金额：$amount
- 分类：$category
- 商户：${payee ?: "未知"}

只返回备注内容，不要其他内容。
            """.trimIndent()

            val response = service.chatCompletion(
                authorization = "Bearer $apiKey",
                request = ChatRequest(
                    model = config.model,
                    messages = listOf(Message("user", prompt)),
                    temperature = 0.5,
                    maxTokens = 30
                )
            )

            response.choices?.firstOrNull()?.message?.content?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
