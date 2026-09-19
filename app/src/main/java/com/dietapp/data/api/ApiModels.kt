package com.dietapp.data.api

import com.google.gson.annotations.SerializedName

// ========== 饮食记录 ==========

data class FoodItem(
    val name: String,
    val amount: Double = 1.0,
    val unit: String = "份",
    @SerializedName("weight_g") val weightGrams: Double = 0.0,
    val cal: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

data class DietRecord(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 1,
    val date: String = "",
    @SerializedName("meal_type") val mealType: String = "",
    val foods: List<FoodItem> = emptyList(),
    @SerializedName("total_cal") val totalCal: Double = 0.0,
    @SerializedName("total_protein") val totalProtein: Double = 0.0,
    @SerializedName("total_carbs") val totalCarbs: Double = 0.0,
    @SerializedName("total_fat") val totalFat: Double = 0.0,
    @SerializedName("ai_report") val aiReport: String = "",
    @SerializedName("photo_url") val photoUrl: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

data class DietRecordRequest(
    val date: String,
    @SerializedName("meal_type") val mealType: String,
    val foods: List<FoodItem>,
    @SerializedName("total_cal") val totalCal: Double,
    @SerializedName("total_protein") val totalProtein: Double,
    @SerializedName("total_carbs") val totalCarbs: Double,
    @SerializedName("total_fat") val totalFat: Double,
    @SerializedName("ai_report") val aiReport: String,
    @SerializedName("photo_url") val photoUrl: String = ""
)

data class DietListResponse(
    val records: List<DietRecord> = emptyList()
)

// ========== 每日汇总 ==========

data class DailySummary(
    val date: String = "",
    val intake: NutritionData = NutritionData(),
    @SerializedName("exercise_burned") val exerciseBurned: Double = 0.0,
    @SerializedName("net_cal") val netCal: Double = 0.0
)

data class NutritionData(
    @SerializedName("total_cal") val totalCal: Double = 0.0,
    @SerializedName("total_protein") val totalProtein: Double = 0.0,
    @SerializedName("total_carbs") val totalCarbs: Double = 0.0,
    @SerializedName("total_fat") val totalFat: Double = 0.0
)

// ========== 体脂数据 ==========

data class BodyRecord(
    val id: Int = 0,
    val weight: Double = 0.0,
    @SerializedName("body_fat") val bodyFat: Double = 0.0,
    val bmi: Double = 0.0,
    @SerializedName("measured_at") val measuredAt: String = ""
)

data class BodyTrend(
    val date: String = "",
    val weight: Double = 0.0,
    @SerializedName("body_fat") val bodyFat: Double = 0.0,
    val bmi: Double = 0.0
)

// ========== 运动记录 ==========

data class ExerciseRecord(
    val id: Int = 0,
    val date: String = "",
    @SerializedName("exercise_type") val exerciseType: String = "",
    @SerializedName("duration_min") val durationMin: Int = 0,
    @SerializedName("calories_burned") val caloriesBurned: Int = 0
)

data class ExerciseRequest(
    val date: String,
    @SerializedName("exercise_type") val exerciseType: String,
    @SerializedName("duration_min") val durationMin: Int,
    @SerializedName("weight_kg") val weightKg: Double? = null
)

data class CalcExerciseRequest(
    val type: String,
    @SerializedName("duration_min") val durationMin: Int
)

data class CalcExerciseResponse(
    val type: String = "",
    @SerializedName("duration_min") val durationMin: Int = 0,
    val met: Double = 0.0,
    @SerializedName("calories_burned") val caloriesBurned: Int = 0
)

// ========== DeepSeek ==========

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>
)

data class DeepSeekResponse(
    val choices: List<DeepSeekChoice> = emptyList()
)

data class DeepSeekChoice(
    val message: DeepSeekMessage? = null
)

// ========== 百度 ==========

data class BaiduRequest(
    val image: String,
    @SerializedName("top_num") val topNum: Int = 5
)

data class BaiduResponse(
    val result: List<BaiduResult> = emptyList()
)

data class BaiduResult(
    val name: String = "",
    val probability: Double = 0.0,
    val calorie: String = ""
)

data class DeleteFoodRequest(
    @SerializedName("food_index") val foodIndex: Int
)

// ========== 通用 ==========

data class ApiResponse(
    val id: Int = 0,
    val message: String = "",
    val error: String = ""
)
