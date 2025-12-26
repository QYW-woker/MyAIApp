package com.myaiapp.data.import

import android.content.Context
import android.net.Uri
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 账单导入器
 * 支持微信和支付宝CSV账单导入
 */
class BillImporter(
    private val context: Context,
    private val storageManager: FileStorageManager
) {

    sealed class ImportResult {
        data class Success(val count: Int, val skipped: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    /**
     * 从CSV文件导入账单
     */
    suspend fun importFromCsv(
        uri: Uri,
        bookId: String,
        accountId: String,
        source: BillSource
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult.Error("无法打开文件")

            val reader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            val lines = reader.readAll()
            reader.close()

            if (lines.isEmpty()) {
                return@withContext ImportResult.Error("文件为空")
            }

            val categories = storageManager.getCategories()
            val transactions = mutableListOf<Transaction>()
            var skipped = 0

            // 跳过表头行
            val dataStartIndex = findDataStartIndex(lines, source)

            for (i in dataStartIndex until lines.size) {
                val row = lines[i]
                try {
                    val transaction = when (source) {
                        BillSource.WECHAT -> parseWechatRow(row, bookId, accountId, categories)
                        BillSource.ALIPAY -> parseAlipayRow(row, bookId, accountId, categories)
                    }
                    if (transaction != null) {
                        transactions.add(transaction)
                    } else {
                        skipped++
                    }
                } catch (e: Exception) {
                    skipped++
                }
            }

            // 批量保存交易
            if (transactions.isNotEmpty()) {
                val existing = storageManager.getTransactions(bookId)
                storageManager.saveTransactions(bookId, existing + transactions)
            }

            ImportResult.Success(transactions.size, skipped)
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "导入失败")
        }
    }

    private fun findDataStartIndex(lines: List<Array<String>>, source: BillSource): Int {
        // 微信账单通常前16行是统计信息，实际数据从第17行开始
        // 支付宝账单通常前4行是表头
        return when (source) {
            BillSource.WECHAT -> {
                lines.indexOfFirst { row ->
                    row.any { it.contains("交易时间") || it.contains("交易类型") }
                } + 1
            }
            BillSource.ALIPAY -> {
                lines.indexOfFirst { row ->
                    row.any { it.contains("交易时间") || it.contains("交易号") }
                } + 1
            }
        }.coerceAtLeast(1)
    }

    /**
     * 解析微信账单行
     * 微信账单格式: 交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注
     */
    private fun parseWechatRow(
        row: Array<String>,
        bookId: String,
        accountId: String,
        categories: List<Category>
    ): Transaction? {
        if (row.size < 6) return null

        val timeStr = row.getOrNull(0)?.trim() ?: return null
        val transactionType = row.getOrNull(1)?.trim() ?: ""
        val counterparty = row.getOrNull(2)?.trim() ?: ""
        val product = row.getOrNull(3)?.trim() ?: ""
        val incomeExpense = row.getOrNull(4)?.trim() ?: return null
        val amountStr = row.getOrNull(5)?.trim()?.replace("¥", "")?.replace(",", "") ?: return null
        val status = row.getOrNull(7)?.trim() ?: ""

        // 跳过非成功交易
        if (!status.contains("支付成功") && !status.contains("已收钱") && !status.contains("已存入")) {
            return null
        }

        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val type = when {
            incomeExpense.contains("支出") -> TransactionType.EXPENSE
            incomeExpense.contains("收入") -> TransactionType.INCOME
            else -> return null // 跳过不明确的交易
        }

        val date = parseDate(timeStr) ?: System.currentTimeMillis()

        // 智能匹配分类
        val categoryId = matchCategory(counterparty + product, type, categories)

        val note = buildString {
            if (counterparty.isNotEmpty()) append(counterparty)
            if (product.isNotEmpty() && product != "/" && product != counterparty) {
                if (isNotEmpty()) append(" - ")
                append(product)
            }
        }.take(50)

        return Transaction(
            type = type,
            amount = amount,
            categoryId = categoryId,
            accountId = accountId,
            bookId = bookId,
            date = date,
            note = note
        )
    }

    /**
     * 解析支付宝账单行
     * 支付宝账单格式: 交易时间,交易分类,交易对方,商品说明,收/支,金额,收/付款方式,交易状态,交易订单号,商家订单号,备注
     */
    private fun parseAlipayRow(
        row: Array<String>,
        bookId: String,
        accountId: String,
        categories: List<Category>
    ): Transaction? {
        if (row.size < 6) return null

        val timeStr = row.getOrNull(0)?.trim() ?: return null
        val categoryName = row.getOrNull(1)?.trim() ?: ""
        val counterparty = row.getOrNull(2)?.trim() ?: ""
        val product = row.getOrNull(3)?.trim() ?: ""
        val incomeExpense = row.getOrNull(4)?.trim() ?: return null
        val amountStr = row.getOrNull(5)?.trim()?.replace(",", "") ?: return null
        val status = row.getOrNull(7)?.trim() ?: ""

        // 跳过非成功交易
        if (status.contains("退款") || status.contains("关闭")) {
            return null
        }

        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val type = when {
            incomeExpense.contains("支出") -> TransactionType.EXPENSE
            incomeExpense.contains("收入") -> TransactionType.INCOME
            else -> return null
        }

        val date = parseDate(timeStr) ?: System.currentTimeMillis()

        // 智能匹配分类
        val categoryId = matchCategory(categoryName + counterparty + product, type, categories)

        val note = buildString {
            if (counterparty.isNotEmpty()) append(counterparty)
            if (product.isNotEmpty() && product != counterparty) {
                if (isNotEmpty()) append(" - ")
                append(product)
            }
        }.take(50)

        return Transaction(
            type = type,
            amount = amount,
            categoryId = categoryId,
            accountId = accountId,
            bookId = bookId,
            date = date,
            note = note
        )
    }

    private fun parseDate(dateStr: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.CHINA)
                return sdf.parse(dateStr)?.time
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    /**
     * 根据关键词智能匹配分类
     */
    private fun matchCategory(
        text: String,
        type: TransactionType,
        categories: List<Category>
    ): String {
        val lowerText = text.lowercase()

        val typeCategories = categories.filter { it.type == type }

        // 关键词匹配规则
        val keywordMap = mapOf(
            // 支出
            "餐" to "exp_food",
            "饭" to "exp_food",
            "食" to "exp_food",
            "吃" to "exp_food",
            "美团" to "exp_food",
            "饿了么" to "exp_food",
            "肯德基" to "exp_food",
            "麦当劳" to "exp_food",
            "星巴克" to "exp_food",
            "奶茶" to "exp_food",
            "咖啡" to "exp_food",
            "超市" to "exp_shopping",
            "商城" to "exp_shopping",
            "淘宝" to "exp_shopping",
            "京东" to "exp_shopping",
            "拼多多" to "exp_shopping",
            "天猫" to "exp_shopping",
            "购物" to "exp_shopping",
            "滴滴" to "exp_transport",
            "地铁" to "exp_transport",
            "公交" to "exp_transport",
            "出行" to "exp_transport",
            "打车" to "exp_transport",
            "加油" to "exp_transport",
            "停车" to "exp_transport",
            "电影" to "exp_entertainment",
            "游戏" to "exp_entertainment",
            "视频" to "exp_entertainment",
            "会员" to "exp_entertainment",
            "房租" to "exp_housing",
            "水电" to "exp_housing",
            "物业" to "exp_housing",
            "医院" to "exp_medical",
            "药" to "exp_medical",
            "教育" to "exp_education",
            "培训" to "exp_education",
            "话费" to "exp_communication",
            "流量" to "exp_communication",
            "充值" to "exp_communication",
            // 收入
            "工资" to "inc_salary",
            "薪" to "inc_salary",
            "奖金" to "inc_bonus",
            "红包" to "inc_gift",
            "转账" to "inc_other",
            "退款" to "inc_refund",
            "利息" to "inc_interest"
        )

        for ((keyword, categoryId) in keywordMap) {
            if (lowerText.contains(keyword)) {
                val category = typeCategories.find { it.id == categoryId }
                if (category != null) {
                    return category.id
                }
            }
        }

        // 默认分类
        return typeCategories.find { it.id.contains("other") }?.id
            ?: typeCategories.firstOrNull()?.id
            ?: if (type == TransactionType.EXPENSE) "exp_other" else "inc_other"
    }
}

enum class BillSource {
    WECHAT,  // 微信
    ALIPAY   // 支付宝
}
