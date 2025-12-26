package com.myaiapp.data.voice

import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType

/**
 * 语音解析结果
 */
data class VoiceParseResult(
    val amount: Double? = null,
    val categoryId: String? = null,
    val note: String? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE
)

/**
 * 语音解析器
 * 从语音文本中提取记账信息
 */
class VoiceParser {

    // 中文数字映射
    private val chineseNumbers = mapOf(
        '零' to 0, '一' to 1, '二' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9,
        '两' to 2
    )

    // 中文单位
    private val chineseUnits = mapOf(
        '十' to 10, '百' to 100, '千' to 1000, '万' to 10000
    )

    /**
     * 解析语音文本
     */
    fun parse(text: String, categories: List<Category>): VoiceParseResult {
        val normalizedText = text.replace(" ", "")

        // 判断收入还是支出
        val transactionType = when {
            normalizedText.contains("收入") ||
            normalizedText.contains("收到") ||
            normalizedText.contains("赚了") ||
            normalizedText.contains("工资") ||
            normalizedText.contains("奖金") -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 提取金额
        val amount = extractAmount(normalizedText)

        // 匹配分类
        val categoryId = matchCategory(normalizedText, categories, transactionType)

        // 提取备注（移除金额和分类相关的词）
        val note = extractNote(normalizedText, amount, categories)

        return VoiceParseResult(
            amount = amount,
            categoryId = categoryId,
            note = note,
            transactionType = transactionType
        )
    }

    /**
     * 提取金额
     */
    private fun extractAmount(text: String): Double? {
        // 先尝试匹配阿拉伯数字金额
        val arabicPattern = Regex("(\\d+(?:\\.\\d+)?)[元块钱]?")
        arabicPattern.find(text)?.let { match ->
            return match.groupValues[1].toDoubleOrNull()
        }

        // 尝试匹配中文数字金额
        val chinesePattern = Regex("([零一二三四五六七八九十百千万两]+)[元块钱]")
        chinesePattern.find(text)?.let { match ->
            return convertChineseNumber(match.groupValues[1])
        }

        // 尝试匹配纯数字
        val pureNumberPattern = Regex("(\\d+(?:\\.\\d+)?)")
        pureNumberPattern.find(text)?.let { match ->
            return match.groupValues[1].toDoubleOrNull()
        }

        return null
    }

    /**
     * 转换中文数字
     */
    private fun convertChineseNumber(chinese: String): Double? {
        if (chinese.isEmpty()) return null

        var result = 0
        var temp = 0
        var lastUnit = 1

        for (char in chinese) {
            when {
                chineseNumbers.containsKey(char) -> {
                    temp = chineseNumbers[char]!!
                }
                chineseUnits.containsKey(char) -> {
                    val unit = chineseUnits[char]!!
                    if (temp == 0 && char == '十') temp = 1
                    if (unit >= 10000) {
                        result = (result + temp) * unit
                        temp = 0
                    } else {
                        result += temp * unit
                        temp = 0
                    }
                    lastUnit = unit
                }
            }
        }

        result += temp

        return if (result > 0) result.toDouble() else null
    }

    /**
     * 匹配分类
     */
    private fun matchCategory(
        text: String,
        categories: List<Category>,
        transactionType: TransactionType
    ): String? {
        // 关键词到分类的映射
        val keywordMappings = mapOf(
            // 餐饮
            listOf("吃饭", "午餐", "晚餐", "早餐", "餐", "外卖", "饭", "美食", "火锅", "烧烤", "奶茶", "咖啡", "饮料") to "餐饮",
            // 交通
            listOf("打车", "出租车", "地铁", "公交", "加油", "停车", "高铁", "火车", "飞机", "机票", "滴滴", "网约车") to "交通",
            // 购物
            listOf("买", "购物", "淘宝", "京东", "拼多多", "超市", "商场", "衣服", "鞋子") to "购物",
            // 娱乐
            listOf("电影", "游戏", "KTV", "唱歌", "玩", "旅游", "门票") to "娱乐",
            // 日用
            listOf("日用", "日常", "生活用品", "水电", "物业", "房租") to "日用",
            // 医疗
            listOf("医院", "药", "看病", "挂号", "体检") to "医疗",
            // 通讯
            listOf("话费", "流量", "手机", "充值") to "通讯",
            // 工资
            listOf("工资", "薪水", "薪资") to "工资",
            // 奖金
            listOf("奖金", "年终奖", "绩效") to "奖金",
            // 理财
            listOf("利息", "股票", "基金", "理财", "收益") to "理财"
        )

        // 根据交易类型过滤分类
        val filteredCategories = categories.filter { it.type == transactionType }

        // 尝试通过关键词匹配
        for ((keywords, categoryName) in keywordMappings) {
            if (keywords.any { text.contains(it) }) {
                filteredCategories.find { it.name.contains(categoryName) }?.let {
                    return it.id
                }
            }
        }

        // 直接匹配分类名称
        filteredCategories.find { text.contains(it.name) }?.let {
            return it.id
        }

        return null
    }

    /**
     * 提取备注
     */
    private fun extractNote(
        text: String,
        amount: Double?,
        categories: List<Category>
    ): String? {
        var note = text

        // 移除金额相关的词
        note = note.replace(Regex("\\d+(?:\\.\\d+)?[元块钱]?"), "")
        note = note.replace(Regex("[零一二三四五六七八九十百千万两]+[元块钱]"), "")

        // 移除常见的动词和介词
        val removeWords = listOf("花了", "花", "用了", "用", "买了", "买", "支出", "收入", "收到", "赚了")
        removeWords.forEach { word ->
            note = note.replace(word, "")
        }

        // 移除分类名称
        categories.forEach { category ->
            note = note.replace(category.name, "")
        }

        note = note.trim()

        return if (note.isNotEmpty() && note.length > 1) note else null
    }
}
