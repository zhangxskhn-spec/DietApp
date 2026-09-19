package com.dietapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * RAG 食材知识库
 * 从 assets/food_knowledge_base.json 加载
 */
data class FoodKnowledge(
    val name: String,
    val category: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,
    val fiber: Double = 0.0,
    val unit: String = "",
    val tags: List<String> = emptyList(),
    val description: String = ""
)

class FoodKnowledgeBase(private val context: Context) {
    private val knowledgeList: MutableList<FoodKnowledge> = mutableListOf()
    private val gson = Gson()

    init {
        loadFromAssets()
    }

    private fun loadFromAssets() {
        try {
            context.assets.open("food_knowledge_base.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<FoodKnowledge>>() {}.type
                    val list: List<FoodKnowledge> = gson.fromJson(reader, type)
                    knowledgeList.addAll(list)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 按 name 建索引，搜索时 O(1) 查找
    private val nameIndex: Map<String, FoodKnowledge> by lazy {
        knowledgeList.associateBy { it.name.lowercase() }
    }

    private val charIndex: Map<Char, List<FoodKnowledge>> by lazy {
        buildMap {
            for (food in knowledgeList) {
                for (ch in food.name) {
                    getOrPut(ch) { mutableListOf() }.let { (it as MutableList).add(food) }
                }
            }
        }
    }

    /**
     * 搜索食材知识
     * 支持模糊匹配：输入"蒜蓉排骨"匹配"排骨"，输入"梨"匹配"梨"
     */
    fun search(keyword: String): List<FoodKnowledge> {
        if (keyword.isBlank()) return emptyList()
        val kw = keyword.trim().lowercase()
        // 1. 精确匹配
        nameIndex[kw]?.let { return listOf(it) }
        // 2. 关键词包含知识库中的名称（如"蒜蓉排骨"→"排骨"）
        val byKwContains = knowledgeList.filter { kw.contains(it.name, ignoreCase = true) }
        if (byKwContains.isNotEmpty()) return byKwContains
        // 3. 知识库名称包含关键词（如"香芋"→"芋头"）
        val firstChar = kw.first()
        val candidates = charIndex[firstChar] ?: knowledgeList
        return candidates.filter { it.name.contains(kw, ignoreCase = true) }
    }

    /**
     * 从一句话中提取食材并检索
     * "我吃了一碗芋头蒸排骨" → ["芋头", "排骨"]
     */
    fun searchFromText(text: String): List<FoodKnowledge> {
        val results = mutableListOf<FoodKnowledge>()
        for (food in knowledgeList) {
            if (text.contains(food.name)) {
                results.add(food)
            }
        }
        return results
    }

    /**
     * 构建 RAG Prompt
     */
    fun buildRagPrompt(userInput: String): String {
        val matchedFoods = searchFromText(userInput)
        val sb = StringBuilder()

        sb.appendLine("【食材权威数据】")
        if (matchedFoods.isNotEmpty()) {
            for (food in matchedFoods) {
                sb.appendLine("- ${food.name}（每100g）：热量${food.calories}kcal，蛋白质${food.protein}g，脂肪${food.fat}g，碳水${food.carbs}g，膳食纤维${food.fiber}g")
                sb.appendLine("  分类：${food.category}")
                if (food.description.isNotBlank()) {
                    sb.appendLine("  说明：${food.description}")
                }
            }
        } else {
            sb.appendLine("（知识库中未找到完全匹配的食材，请根据您的专业知识回答）")
        }
        sb.appendLine()
        sb.appendLine("【用户饮食记录】")
        sb.appendLine(userInput)
        sb.appendLine()
        sb.appendLine("请逐项分析：每种食物写清份量、估算克数(weight_g)、热量与三大营养素；最后给80字以上的具体饮食建议。")
        sb.appendLine("JSON 中每项需含 weight_g（可食部克数），如：{\"name\":\"米饭\",\"amount\":1,\"unit\":\"碗\",\"weight_g\":250,...}")

        return sb.toString()
    }

    fun size(): Int = knowledgeList.size
}
