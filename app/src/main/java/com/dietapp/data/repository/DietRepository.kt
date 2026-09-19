package com.dietapp.data.repository

import com.dietapp.data.api.*

object DietRepository {
    private val api = RetrofitClient.apiService

    // ========== 饮食 ==========
    suspend fun getTodayRecords(date: String) = api.getDietRecords(date = date)
    suspend fun getDateRange(start: String, end: String) = api.getDietRange(start, end)
    suspend fun addRecord(request: DietRecordRequest) = api.addDietRecord(request)
    suspend fun deleteRecord(id: Int) = api.deleteDietRecord(id)
    suspend fun deleteFoodItem(recordId: Int, foodIndex: Int) = api.deleteFoodItem(recordId, DeleteFoodRequest(foodIndex))
    suspend fun getSummary(date: String) = api.getDailySummary(date)

    // ========== 体脂 ==========
    suspend fun getLatestBody() = api.getLatestBody()
    suspend fun getBodyTrend(days: Int = 7) = api.getBodyTrend(days)

    // ========== 运动 ==========
    suspend fun getExercises(date: String) = api.getExercises(date)
    suspend fun addExercise(request: ExerciseRequest) = api.addExercise(request)
    suspend fun deleteExercise(id: Int) = api.deleteExercise(id)
    suspend fun calcExercise(type: String, minutes: Int) = api.calcExercise(
        CalcExerciseRequest(type, minutes)
    )
    suspend fun getExerciseTypes() = api.getExerciseTypes()

    // ========== AI ==========
    suspend fun aiChat(system: String, user: String): String {
        val request = DeepSeekRequest(
            messages = listOf(
                DeepSeekMessage("system", system),
                DeepSeekMessage("user", user)
            )
        )
        val response = api.deepSeekChat(request)
        return response.choices.firstOrNull()?.message?.content ?: ""
    }

    // 多轮对话（带上下文历史）
    suspend fun deepSeekChat(messages: List<Map<String, String>>): String {
        val deepseekMessages = messages.map {
            DeepSeekMessage(it["role"] ?: "user", it["content"] ?: "")
        }
        val request = DeepSeekRequest(
            messages = deepseekMessages
        )
        val response = api.deepSeekChat(request)
        return response.choices.firstOrNull()?.message?.content ?: ""
    }

    // ========== 百度识别 ==========
    suspend fun recognizeFood(base64Image: String) = api.baiduRecognize(
        BaiduRequest(image = base64Image)
    )
}
