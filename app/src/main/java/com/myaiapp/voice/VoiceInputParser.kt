package com.myaiapp.voice

import com.myaiapp.data.local.model.Category
import com.myaiapp.data.local.model.TransactionType
import java.util.regex.Pattern

/**
 * 语音输入解析器
 * 将语音识别的文字解析为记账信息
 */
class VoiceInputParser {

    /**
     * 解析结果
     */
    data class ParseResult(
        val amount: Double? = null,
        val note: String = "",
        val type: TransactionType = TransactionType.EXPENSE,
        val categoryKeyword: String = "",
        val confidence: Float = 0f
    )

    companion object {
        // 中文数字映射
        private val chineseNumbers = mapOf(
            '零' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4,
            '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9, '十' to 10,
            '百' to 100, '千' to 1000, '万' to 10000
        )

        // 收入关键词
        private val incomeKeywords = listOf(
            "收入", "收到", "收款", "入账", "进账", "收益", "工资", "奖金",
            "红包", "转入", "利息", "报销", "退款", "卖"
        )

        // 支出关键词
        private val expenseKeywords = listOf(
            "花", "花了", "消费", "支出", "买", "付款", "付了", "给", "交",
            "充值", "缴费", "转账", "转出", "打车", "吃饭", "购物"
        )

        // 分类关键词映射
        private val categoryKeywords = mapOf(
            // 餐饮
            "吃" to "餐饮", "饭" to "餐饮", "餐" to "餐饮", "外卖" to "餐饮",
            "早餐" to "餐饮", "午餐" to "餐饮", "晚餐" to "餐饮", "宵夜" to "餐饮",
            "喝" to "餐饮", "咖啡" to "餐饮", "奶茶" to "餐饮", "饮料" to "餐饮",

            // 交通
            "打车" to "交通", "出租车" to "交通", "滴滴" to "交通", "公交" to "交通",
            "地铁" to "交通", "高铁" to "交通", "火车" to "交通", "飞机" to "交通",
            "机票" to "交通", "加油" to "交通", "油费" to "交通", "停车" to "交通",

            // 购物
            "买" to "购物", "购物" to "购物", "淘宝" to "购物", "京东" to "购物",
            "拼多多" to "购物", "衣服" to "购物", "鞋" to "购物", "超市" to "购物",

            // 娱乐
            "电影" to "娱乐", "游戏" to "娱乐", "充值" to "娱乐", "KTV" to "娱乐",
            "唱歌" to "娱乐", "健身" to "娱乐", "旅游" to "娱乐", "景点" to "娱乐",

            // 居住
            "房租" to "居住", "物业" to "居住", "水费" to "居住", "电费" to "居住",
            "燃气" to "居住", "网费" to "居住", "宽带" to "居住",

            // 医疗
            "医院" to "医疗", "看病" to "医疗", "药" to "医疗", "挂号" to "医疗",
            "体检" to "医疗",

            // 通讯
            "话费" to "通讯", "流量" to "通讯", "手机" to "通讯",

            // 工资
            "工资" to "工资", "薪水" to "工资", "工钱" to "工资",

            // 奖金
            "奖金" to "奖金", "年终奖" to "奖金", "绩效" to "奖金",

            // 红包
            "红包" to "红包", "微信红包" to "红包", "支付宝红包" to "红包"
        )
    }

    /**
     * 解析语音输入
     */
    fun parse(input: String): ParseResult {
        if (input.isBlank()) {
            return ParseResult()
        }

        val cleanInput = input.trim()

        // 解析金额
        val amount = parseAmount(cleanInput)

        // 解析类型
        val type = parseType(cleanInput)

        // 解析分类关键词
        val categoryKeyword = parseCategoryKeyword(cleanInput)

        // 提取备注
        val note = extractNote(cleanInput, amount)

        // 计算置信度
        val confidence = calculateConfidence(amount, categoryKeyword)

        return ParseResult(
            amount = amount,
            note = note,
            type = type,
            categoryKeyword = categoryKeyword,
            confidence = confidence
        )
    }

    /**
     * 解析金额
     */
    private fun parseAmount(input: String): Double? {
        // 尝试匹配阿拉伯数字金额
        val arabicPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(元|块|毛|角|分|¥|￥)?")
        val arabicMatcher = arabicPattern.matcher(input)

        if (arabicMatcher.find()) {
            val numberStr = arabicMatcher.group(1)
            val unit = arabicMatcher.group(2) ?: ""

            var amount = numberStr?.toDoubleOrNull() ?: return null

            // 处理单位
            when (unit) {
                "毛", "角" -> amount *= 0.1
                "分" -> amount *= 0.01
            }

            return amount
        }

        // 尝试匹配中文数字金额
        return parseChineseNumber(input)
    }

    /**
     * 解析中文数字
     */
    private fun parseChineseNumber(input: String): Double? {
        // 匹配中文数字模式，如：五十块、一百二十元
        val chinesePattern = Pattern.compile("([零一二两三四五六七八九十百千万]+)\\s*(元|块|毛|角|分)?")
        val chineseMatcher = chinesePattern.matcher(input)

        if (chineseMatcher.find()) {
            val chineseNum = chineseMatcher.group(1) ?: return null
            val unit = chineseMatcher.group(2) ?: ""

            var amount = convertChineseToNumber(chineseNum).toDouble()

            when (unit) {
                "毛", "角" -> amount *= 0.1
                "分" -> amount *= 0.01
            }

            return if (amount > 0) amount else null
        }

        return null
    }

    /**
     * 转换中文数字为阿拉伯数字
     */
    private fun convertChineseToNumber(chinese: String): Int {
        if (chinese.isEmpty()) return 0

        var result = 0
        var temp = 0
        var lastUnit = 1

        for (char in chinese) {
            val value = chineseNumbers[char] ?: continue

            when {
                value >= 10 -> {
                    if (temp == 0) temp = 1
                    temp *= value
                    if (value >= lastUnit) {
                        result += temp
                        temp = 0
                    }
                    lastUnit = value
                }
                else -> {
                    temp = value
                }
            }
        }

        return result + temp
    }

    /**
     * 解析交易类型
     */
    private fun parseType(input: String): TransactionType {
        // 检查是否包含收入关键词
        for (keyword in incomeKeywords) {
            if (input.contains(keyword)) {
                return TransactionType.INCOME
            }
        }

        // 默认为支出
        return TransactionType.EXPENSE
    }

    /**
     * 解析分类关键词
     */
    private fun parseCategoryKeyword(input: String): String {
        for ((keyword, category) in categoryKeywords) {
            if (input.contains(keyword)) {
                return category
            }
        }
        return ""
    }

    /**
     * 匹配最佳分类
     */
    fun matchCategory(keyword: String, categories: List<Category>): Category? {
        if (keyword.isBlank()) return null

        // 精确匹配
        categories.find { it.name == keyword }?.let { return it }

        // 包含匹配
        categories.find { it.name.contains(keyword) || keyword.contains(it.name) }?.let { return it }

        return null
    }

    /**
     * 提取备注
     */
    private fun extractNote(input: String, amount: Double?): String {
        var note = input

        // 移除金额部分
        if (amount != null) {
            note = note.replace(Regex("\\d+\\.?\\d*\\s*(元|块|毛|角|分|¥|￥)?"), "")
            note = note.replace(Regex("[零一二两三四五六七八九十百千万]+\\s*(元|块|毛|角|分)?"), "")
        }

        // 移除类型关键词
        for (keyword in expenseKeywords + incomeKeywords) {
            note = note.replace(keyword, "")
        }

        // 清理多余空格
        note = note.trim().replace(Regex("\\s+"), " ")

        return note
    }

    /**
     * 计算置信度
     */
    private fun calculateConfidence(amount: Double?, categoryKeyword: String): Float {
        var confidence = 0f

        if (amount != null && amount > 0) {
            confidence += 0.5f
        }

        if (categoryKeyword.isNotBlank()) {
            confidence += 0.3f
        }

        return confidence.coerceAtMost(1f)
    }
}
