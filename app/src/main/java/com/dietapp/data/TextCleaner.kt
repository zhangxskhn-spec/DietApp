package com.dietapp.data

/**
 * AI 返回文本清洗工具
 * 去除多余空格、乱码括号、特殊字符，保证显示整洁
 */
object TextCleaner {

    /**
     * 清理 AI 返回的文本
     * - 多余空格 → 单空格
     * - 多余的括号内容（如空括号、只有空格的括号）
     * - 多余换行 → 适当压缩
     * - 去首尾空白
     */
    fun clean(text: String): String {
        if (text.isBlank()) return text

        return text
            .trim()
            // 压缩多余空格（保留单空格）
            .replace(Regex("\\s{3,}"), "  ")
            // 去除只有空格/标点的空括号
            .replace(Regex("\\(\\s*[,，、.。]?\\s*\\)"), "")
            .replace(Regex("\\[\\s*\\]"), "")
            .replace(Regex("\\{\\s*\\}"), "")
            // 去除多余的空括号对
            .replace(Regex("\\(\\)"), "")
            // 修复中文和英文之间缺少空格 or 多余空格
            .replace(Regex("([\\u4e00-\\u9fff])\\s+([a-zA-Z])"), "$1 $2")
            .replace(Regex("([a-zA-Z])\\s+([\\u4e00-\\u9fff])"), "$1 $2")
            // 压缩连续空行（3行以上→2行）
            .replace(Regex("\\n{3,}"), "\n\n")
            // 去掉行尾多余空格
            .replace(Regex("\\s+$", RegexOption.MULTILINE), "")
            .trim()
    }

    /**
     * 清理 AI 返回的 JSON 字符串
     * 去除 ```json ``` 标记，修复常见格式问题
     */
    fun cleanJson(text: String): String {
        return text
            .replace("```json", "")
            .replace("```", "")
            .replace("`", "")
            .trim()
    }

    /**
     * 从 AI 回复中提取 JSON 对象
     */
    fun extractJson(text: String): String? {
        // 先尝试 【JSON】...【/JSON】格式
        val jsonStart = text.indexOf("【JSON】")
        val jsonEnd = text.indexOf("【/JSON】")
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return text.substring(jsonStart + 6, jsonEnd).trim()
        }

        // 尝试 ```json ... ``` 格式
        val cleanText = cleanJson(text)
        val braceStart = cleanText.indexOf('{')
        val braceEnd = cleanText.lastIndexOf('}')
        if (braceStart >= 0 && braceEnd > braceStart) {
            return cleanText.substring(braceStart, braceEnd + 1)
        }

        return null
    }

    /**
     * 去除 AI 回复中的 JSON 部分，仅保留可读文本
     */
    fun removeJsonPart(text: String): String {
        return text
            .replace(Regex("【JSON】.*?【/JSON】"), "")
            .replace(Regex("```json[\\s\\S]*?```"), "")
            .replace(Regex("```[\\s\\S]*?```"), "")
            .trim()
    }
}
