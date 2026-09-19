package com.dietapp.data

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREFS_NAME = "diet_prefs"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_DAILY_CAL_GOAL = "daily_cal_goal"
    private const val KEY_AGE = "age"
    private const val KEY_GENDER = "gender"

    // 缓存 SharedPreferences 实例，避免每次操作都创建新的
    @Volatile
    private var prefs: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return prefs ?: synchronized(this) {
            prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also { prefs = it }
        }
    }

    fun getHeight(context: Context): Float =
        getPrefs(context).getFloat(KEY_HEIGHT, 170f)

    fun setHeight(context: Context, height: Float) {
        getPrefs(context).edit().putFloat(KEY_HEIGHT, height).apply()
    }

    fun getWeightKg(context: Context): Double =
        getPrefs(context).getFloat(KEY_WEIGHT, 0f).toDouble().coerceAtLeast(0.0)

    fun setWeight(context: Context, weight: Double) {
        getPrefs(context).edit().putFloat(KEY_WEIGHT, weight.toFloat()).apply()
    }

    fun getDailyCalGoal(context: Context): Int =
        getPrefs(context).getInt(KEY_DAILY_CAL_GOAL, 1800)

    fun setDailyCalGoal(context: Context, goal: Int) {
        getPrefs(context).edit().putInt(KEY_DAILY_CAL_GOAL, goal).apply()
    }

    fun getAge(context: Context): Int =
        getPrefs(context).getInt(KEY_AGE, 0)

    fun setAge(context: Context, age: Int) {
        getPrefs(context).edit().putInt(KEY_AGE, age).apply()
    }

    fun getGender(context: Context): String =
        getPrefs(context).getString(KEY_GENDER, "") ?: ""

    fun setGender(context: Context, gender: String) {
        getPrefs(context).edit().putString(KEY_GENDER, gender).apply()
    }
}
