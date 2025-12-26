package com.myaiapp.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * WebDAV 客户端
 * 支持坚果云、NextCloud 等 WebDAV 服务
 */
class WebDavClient(
    private val serverUrl: String,
    private val username: String,
    private val password: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val credentials = Credentials.basic(username, password)

    /**
     * 测试连接
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(serverUrl.trimEnd('/'))
                .header("Authorization", credentials)
                .method("PROPFIND", null)
                .header("Depth", "0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 207) {
                Result.success(true)
            } else {
                Result.failure(Exception("连接失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传文件
     */
    suspend fun upload(
        remotePath: String,
        data: ByteArray,
        contentType: String = "application/octet-stream"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath)

            // 确保父目录存在
            ensureParentDirectory(remotePath)

            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .put(data.toRequestBody(contentType.toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 201 || response.code == 204) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("上传失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 下载文件
     */
    suspend fun download(remotePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.bytes()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("文件内容为空"))
                }
            } else if (response.code == 404) {
                Result.failure(FileNotFoundException("文件不存在: $remotePath"))
            } else {
                Result.failure(Exception("下载失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查文件是否存在
     */
    suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .method("PROPFIND", null)
                .header("Depth", "0")
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful || response.code == 207
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 创建目录
     */
    suspend fun createDirectory(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .method("MKCOL", null)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 201 || response.code == 405) {
                // 405 表示目录已存在
                Result.success(Unit)
            } else {
                Result.failure(Exception("创建目录失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除文件
     */
    suspend fun delete(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .delete()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 204 || response.code == 404) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 确保父目录存在
     */
    private suspend fun ensureParentDirectory(path: String) {
        val parts = path.trim('/').split('/')
        if (parts.size > 1) {
            var currentPath = ""
            for (i in 0 until parts.size - 1) {
                currentPath += "/${parts[i]}"
                createDirectory(currentPath)
            }
        }
    }

    private fun buildUrl(path: String): String {
        val base = serverUrl.trimEnd('/')
        val remotePath = path.trimStart('/')
        return "$base/$remotePath"
    }

    class FileNotFoundException(message: String) : Exception(message)
}
