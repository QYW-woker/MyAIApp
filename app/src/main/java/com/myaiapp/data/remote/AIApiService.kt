package com.myaiapp.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * AI API 服务接口
 * 支持 DeepSeek 和 Groq API
 */
interface AIApiService {

    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Double = 0.3,
    @SerializedName("max_tokens")
    val maxTokens: Int = 500
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: Message?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

// 交易解析结果
data class TransactionParseResult(
    val amount: Double?,
    val payee: String?,
    val paymentMethod: String?,
    val date: String?,
    val category: String?,
    val note: String?
)
