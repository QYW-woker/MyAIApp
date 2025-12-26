package com.myaiapp.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Transaction
import com.myaiapp.data.local.model.TransactionType
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出格式
 */
enum class ExportFormat {
    CSV,
    EXCEL,
    PDF
}

/**
 * 导出时间范围
 */
enum class ExportDateRange {
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    ALL,
    CUSTOM
}

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(val file: File, val uri: Uri) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * 数据导出管理器
 */
class ExportManager(private val context: Context) {

    private val storageManager = FileStorageManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 导出交易记录为CSV
     */
    suspend fun exportToCsv(
        transactions: List<Transaction>,
        fileName: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val actualFileName = fileName ?: "记账数据_${fileDateFormat.format(Date())}.csv"
            val file = createExportFile(actualFileName)

            FileWriter(file).use { fw ->
                CSVWriter(fw).use { writer ->
                    // 写入表头
                    writer.writeNext(arrayOf(
                        "日期", "类型", "分类", "金额", "账户", "备注", "标签"
                    ))

                    // 获取分类和账户信息
                    val categories = storageManager.getCategories()
                    val accounts = storageManager.getAssetAccounts()

                    // 写入数据
                    transactions.sortedByDescending { it.date }.forEach { transaction ->
                        val category = categories.find { it.id == transaction.categoryId }
                        val account = accounts.find { it.id == transaction.accountId }

                        val typeStr = when (transaction.type) {
                            TransactionType.EXPENSE -> "支出"
                            TransactionType.INCOME -> "收入"
                            TransactionType.TRANSFER -> "转账"
                        }

                        val amountStr = if (transaction.type == TransactionType.EXPENSE) {
                            "-${transaction.amount}"
                        } else {
                            "+${transaction.amount}"
                        }

                        writer.writeNext(arrayOf(
                            dateFormat.format(Date(transaction.date)),
                            typeStr,
                            category?.name ?: "未知分类",
                            amountStr,
                            account?.name ?: "未知账户",
                            transaction.note,
                            transaction.tags.joinToString(", ")
                        ))
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ExportResult.Success(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("导出CSV失败: ${e.message}")
        }
    }

    /**
     * 导出交易记录为Excel格式（使用CSV兼容格式）
     */
    suspend fun exportToExcel(
        transactions: List<Transaction>,
        fileName: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val actualFileName = fileName ?: "记账数据_${fileDateFormat.format(Date())}.csv"
            val file = createExportFile(actualFileName)

            // 使用带BOM的UTF-8编码，确保Excel正确识别中文
            FileOutputStream(file).use { fos ->
                // 写入BOM
                fos.write(0xEF)
                fos.write(0xBB)
                fos.write(0xBF)

                val sb = StringBuilder()

                // 写入表头
                sb.appendLine("日期,类型,分类,金额,账户,备注,标签")

                // 获取分类和账户信息
                val categories = storageManager.getCategories()
                val accounts = storageManager.getAssetAccounts()

                // 统计信息
                var totalExpense = 0.0
                var totalIncome = 0.0

                // 写入数据
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    val account = accounts.find { it.id == transaction.accountId }

                    val typeStr = when (transaction.type) {
                        TransactionType.EXPENSE -> {
                            totalExpense += transaction.amount
                            "支出"
                        }
                        TransactionType.INCOME -> {
                            totalIncome += transaction.amount
                            "收入"
                        }
                        TransactionType.TRANSFER -> "转账"
                    }

                    val amountStr = if (transaction.type == TransactionType.EXPENSE) {
                        "-${transaction.amount}"
                    } else {
                        "+${transaction.amount}"
                    }

                    sb.appendLine(
                        "${dateFormat.format(Date(transaction.date))}," +
                        "$typeStr," +
                        "\"${category?.name ?: "未知分类"}\"," +
                        "$amountStr," +
                        "\"${account?.name ?: "未知账户"}\"," +
                        "\"${transaction.note.replace("\"", "\"\"")}\"," +
                        "\"${transaction.tags.joinToString(", ")}\""
                    )
                }

                // 写入统计行
                sb.appendLine()
                sb.appendLine("统计汇总,,,,,")
                sb.appendLine("总支出,,,-$totalExpense,,,")
                sb.appendLine("总收入,,,+$totalIncome,,,")
                sb.appendLine("净收支,,,${totalIncome - totalExpense},,,")

                fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ExportResult.Success(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("导出Excel失败: ${e.message}")
        }
    }

    /**
     * 生成PDF报告
     */
    suspend fun exportToPdf(
        transactions: List<Transaction>,
        title: String = "记账报告",
        fileName: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val actualFileName = fileName ?: "记账报告_${fileDateFormat.format(Date())}.pdf"
            val file = createExportFile(actualFileName)

            // 获取分类和账户信息
            val categories = storageManager.getCategories()
            val accounts = storageManager.getAssetAccounts()

            // 计算统计数据
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
            val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
            val totalExpense = expenseTransactions.sumOf { it.amount }
            val totalIncome = incomeTransactions.sumOf { it.amount }

            // 按分类统计
            val expenseByCategory = expenseTransactions
                .groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            // 使用iText生成PDF
            com.itextpdf.kernel.pdf.PdfDocument(
                com.itextpdf.kernel.pdf.PdfWriter(file)
            ).use { pdfDoc ->
                com.itextpdf.layout.Document(pdfDoc).use { document ->
                    // 设置中文字体
                    val font = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                        "STSong-Light",
                        "UniGB-UCS2-H"
                    )

                    // 标题
                    document.add(
                        com.itextpdf.layout.element.Paragraph(title)
                            .setFont(font)
                            .setFontSize(24f)
                            .setBold()
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    )

                    // 生成日期
                    document.add(
                        com.itextpdf.layout.element.Paragraph(
                            "生成时间: ${dateFormat.format(Date())}"
                        )
                            .setFont(font)
                            .setFontSize(10f)
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    )

                    document.add(com.itextpdf.layout.element.Paragraph("\n"))

                    // 统计概览
                    document.add(
                        com.itextpdf.layout.element.Paragraph("📊 统计概览")
                            .setFont(font)
                            .setFontSize(16f)
                            .setBold()
                    )

                    val summaryTable = com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f))
                    ).useAllAvailableWidth()

                    summaryTable.addCell(createCell("总收入", font).setBackgroundColor(
                        com.itextpdf.kernel.colors.ColorConstants.GREEN.apply {
                            // 淡绿色背景
                        }
                    ))
                    summaryTable.addCell(createCell("总支出", font))
                    summaryTable.addCell(createCell("净收支", font))

                    summaryTable.addCell(createCell("¥${String.format("%.2f", totalIncome)}", font))
                    summaryTable.addCell(createCell("¥${String.format("%.2f", totalExpense)}", font))
                    summaryTable.addCell(createCell("¥${String.format("%.2f", totalIncome - totalExpense)}", font))

                    document.add(summaryTable)
                    document.add(com.itextpdf.layout.element.Paragraph("\n"))

                    // 支出分类统计
                    if (expenseByCategory.isNotEmpty()) {
                        document.add(
                            com.itextpdf.layout.element.Paragraph("📈 支出分类统计")
                                .setFont(font)
                                .setFontSize(16f)
                                .setBold()
                        )

                        val categoryTable = com.itextpdf.layout.element.Table(
                            com.itextpdf.layout.properties.UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f))
                        ).useAllAvailableWidth()

                        categoryTable.addHeaderCell(createCell("分类", font, true))
                        categoryTable.addHeaderCell(createCell("金额", font, true))
                        categoryTable.addHeaderCell(createCell("占比", font, true))

                        expenseByCategory.take(10).forEach { (categoryId, amount) ->
                            val category = categories.find { it.id == categoryId }
                            val percentage = if (totalExpense > 0) amount / totalExpense * 100 else 0.0

                            categoryTable.addCell(createCell(category?.name ?: "未知分类", font))
                            categoryTable.addCell(createCell("¥${String.format("%.2f", amount)}", font))
                            categoryTable.addCell(createCell("${String.format("%.1f", percentage)}%", font))
                        }

                        document.add(categoryTable)
                        document.add(com.itextpdf.layout.element.Paragraph("\n"))
                    }

                    // 交易明细
                    document.add(
                        com.itextpdf.layout.element.Paragraph("📝 交易明细")
                            .setFont(font)
                            .setFontSize(16f)
                            .setBold()
                    )

                    val detailTable = com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(
                            floatArrayOf(1.5f, 0.8f, 1f, 1f, 1.5f)
                        )
                    ).useAllAvailableWidth()

                    detailTable.addHeaderCell(createCell("日期", font, true))
                    detailTable.addHeaderCell(createCell("类型", font, true))
                    detailTable.addHeaderCell(createCell("分类", font, true))
                    detailTable.addHeaderCell(createCell("金额", font, true))
                    detailTable.addHeaderCell(createCell("备注", font, true))

                    transactions.sortedByDescending { it.date }.take(100).forEach { transaction ->
                        val category = categories.find { it.id == transaction.categoryId }

                        val typeStr = when (transaction.type) {
                            TransactionType.EXPENSE -> "支出"
                            TransactionType.INCOME -> "收入"
                            TransactionType.TRANSFER -> "转账"
                        }

                        val amountStr = if (transaction.type == TransactionType.EXPENSE) {
                            "-¥${String.format("%.2f", transaction.amount)}"
                        } else {
                            "+¥${String.format("%.2f", transaction.amount)}"
                        }

                        detailTable.addCell(createCell(
                            SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                .format(Date(transaction.date)),
                            font
                        ))
                        detailTable.addCell(createCell(typeStr, font))
                        detailTable.addCell(createCell(category?.name ?: "-", font))
                        detailTable.addCell(createCell(amountStr, font))
                        detailTable.addCell(createCell(
                            transaction.note.take(20) + if (transaction.note.length > 20) "..." else "",
                            font
                        ))
                    }

                    document.add(detailTable)

                    if (transactions.size > 100) {
                        document.add(
                            com.itextpdf.layout.element.Paragraph(
                                "（仅显示最近100条记录，共${transactions.size}条）"
                            )
                                .setFont(font)
                                .setFontSize(10f)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                        )
                    }

                    // 页脚
                    document.add(com.itextpdf.layout.element.Paragraph("\n\n"))
                    document.add(
                        com.itextpdf.layout.element.Paragraph(
                            "—— AI智能记账 ——"
                        )
                            .setFont(font)
                            .setFontSize(10f)
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    )
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ExportResult.Success(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("导出PDF失败: ${e.message}")
        }
    }

    private fun createCell(
        text: String,
        font: com.itextpdf.kernel.font.PdfFont,
        isHeader: Boolean = false
    ): com.itextpdf.layout.element.Cell {
        return com.itextpdf.layout.element.Cell()
            .add(
                com.itextpdf.layout.element.Paragraph(text)
                    .setFont(font)
                    .setFontSize(if (isHeader) 11f else 10f)
            )
            .setPadding(5f)
            .apply {
                if (isHeader) {
                    setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    setBold()
                }
            }
    }

    /**
     * 根据时间范围筛选交易
     */
    suspend fun getTransactionsByDateRange(
        range: ExportDateRange,
        customStart: Long? = null,
        customEnd: Long? = null
    ): List<Transaction> {
        val bookId = storageManager.getCurrentBookId()
        val allTransactions = storageManager.getTransactions(bookId)

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        val (startTime, endTime) = when (range) {
            ExportDateRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            ExportDateRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis

                Pair(start, end)
            }
            ExportDateRange.THIS_YEAR -> {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            ExportDateRange.ALL -> {
                Pair(0L, now)
            }
            ExportDateRange.CUSTOM -> {
                Pair(customStart ?: 0L, customEnd ?: now)
            }
        }

        return allTransactions.filter { it.date in startTime..endTime }
    }

    /**
     * 创建导出文件
     */
    private fun createExportFile(fileName: String): File {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return File(exportDir, fileName)
    }

    /**
     * 分享导出文件
     */
    fun shareFile(uri: Uri, mimeType: String = "*/*"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * 获取所有导出文件
     */
    fun getExportedFiles(): List<File> {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports")
        return if (exportDir.exists()) {
            exportDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 删除导出文件
     */
    fun deleteExportFile(file: File): Boolean {
        return file.delete()
    }

    /**
     * 清空所有导出文件
     */
    fun clearAllExports() {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports")
        exportDir.listFiles()?.forEach { it.delete() }
    }
}
