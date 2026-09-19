package com.dietapp.data

import java.util.Calendar

/**
 * 餐次解析：优先用户明示（午餐/【午餐】），其次时段推断。
 */
object MealTypeResolver {

    val KEYS = arrayOf("breakfast", "lunch", "dinner", "snack")
    val LABELS = arrayOf("早餐", "午餐", "晚餐", "加餐")

    fun label(mealType: String): String = when (mealType) {
        "breakfast" -> "早餐"
        "lunch" -> "午餐"
        "dinner" -> "晚餐"
        "snack" -> "加餐"
        else -> mealType
    }

    fun indexOf(mealType: String): Int = KEYS.indexOf(mealType).coerceAtLeast(0)

    /** 从用户输入中识别餐次（【午餐】、午餐、午饭 等） */
    fun parseFromText(text: String): String? {
        if (text.isBlank()) return null
        Regex("""【\s*(早餐|午餐|晚餐|加餐)\s*】""").find(text)?.groupValues?.getOrNull(1)?.let { tag ->
            return when (tag) {
                "早餐" -> "breakfast"
                "午餐" -> "lunch"
                "晚餐" -> "dinner"
                "加餐" -> "snack"
                else -> null
            }
        }
        return when {
            text.contains("早餐") || text.contains("早饭") || text.contains("早点") -> "breakfast"
            text.contains("午餐") || text.contains("午饭") || text.contains("中饭") -> "lunch"
            text.contains("晚餐") || text.contains("晚饭") -> "dinner"
            text.contains("夜宵") && !text.contains("加餐") -> "dinner"
            text.contains("加餐") || text.contains("零食") || text.contains("点心") -> "snack"
            else -> null
        }
    }

    /** 按当前时间推断默认餐次（午餐 11–16，晚餐 17–21） */
    fun inferByHour(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String = when {
        hour in 5..10 -> "breakfast"
        hour in 11..16 -> "lunch"
        hour in 17..21 -> "dinner"
        else -> "snack"
    }

    fun resolve(text: String?, explicit: String? = null, fallbackHour: Int? = null): String {
        explicit?.takeIf { it in KEYS.asList() }?.let { return it }
        parseFromText(text.orEmpty())?.let { return it }
        return inferByHour(fallbackHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    }
}
