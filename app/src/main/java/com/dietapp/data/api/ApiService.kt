package com.dietapp.data.api

import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    // ========== 健康检查 ==========
    @GET("api/health")
    suspend fun healthCheck(): Map<String, Any>

    // ========== 饮食记录 ==========
    @GET("api/diet")
    suspend fun getDietRecords(
        @Query("date") date: String? = null,
        @Query("user_id") userId: Int = 1
    ): DietListResponse

    @GET("api/diet/range")
    suspend fun getDietRange(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("user_id") userId: Int = 1
    ): DietListResponse

    @POST("api/diet")
    suspend fun addDietRecord(@Body request: DietRecordRequest): ApiResponse

    @DELETE("api/diet/{id}")
    suspend fun deleteDietRecord(@Path("id") id: Int): ApiResponse

    @PATCH("api/diet/{id}/food")
    suspend fun deleteFoodItem(@Path("id") id: Int, @Body request: DeleteFoodRequest): ApiResponse

    @GET("api/diet/summary")
    suspend fun getDailySummary(
        @Query("date") date: String,
        @Query("user_id") userId: Int = 1
    ): DailySummary

    // ========== 体脂秤 ==========
    @GET("api/body/latest")
    suspend fun getLatestBody(@Query("user_id") userId: Int = 1): BodyRecord?

    @GET("api/body/trend")
    suspend fun getBodyTrend(
        @Query("days") days: Int = 7,
        @Query("user_id") userId: Int = 1
    ): List<BodyTrend>

    // ========== 运动 ==========
    @GET("api/exercise")
    suspend fun getExercises(
        @Query("date") date: String,
        @Query("user_id") userId: Int = 1
    ): List<ExerciseRecord>

    @POST("api/exercise")
    suspend fun addExercise(@Body request: ExerciseRequest): ApiResponse

    @DELETE("api/exercise/{id}")
    suspend fun deleteExercise(@Path("id") id: Int): ApiResponse

    @POST("api/exercise/calc")
    suspend fun calcExercise(@Body request: CalcExerciseRequest): CalcExerciseResponse

    @GET("api/exercise/types")
    suspend fun getExerciseTypes(): List<String>

    // ========== DeepSeek ==========
    @POST("api/deepseek/chat")
    suspend fun deepSeekChat(@Body request: DeepSeekRequest): DeepSeekResponse

    // ========== 百度识别 ==========
    @POST("api/baidu/recognize")
    suspend fun baiduRecognize(@Body request: BaiduRequest): BaiduResponse

    // ========== 上传图片 ==========
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiResponse
}
