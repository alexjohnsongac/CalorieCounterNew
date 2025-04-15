package com.example.caloriecounternew

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

object CalorieStreakManager {
    private const val PREFS_NAME = "CalorieStreakPrefs"
    private const val KEY_STREAK = "streak_count"
    private const val KEY_LAST_UPDATE_DAY = "last_update_day"


    private fun getCalorieGoal(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("CalorieCounterPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("daily_calorie_goal", 2000)
    }

    private fun getGoalType(context: Context): String {
        val prefs = context.getSharedPreferences("WeightPrefs", Context.MODE_PRIVATE)
        return prefs.getString("goal_type", "Maintain weight") ?: "Maintain weight"
    }

    fun checkAndUpdateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastUpdateDay = prefs.getInt(KEY_LAST_UPDATE_DAY, -1)

        val calorieGoal = getCalorieGoal(context)
        val goalType = getGoalType(context)

        val bufferMinus = when (goalType) {
            "Lose weight" -> 400
            "Gain mass" -> 100
            else -> 300
        }

        val bufferPlus = when (goalType) {
            "Lose weight" -> 100
            "Gain mass" -> 400
            else -> 300
        }

        val minCal = calorieGoal - bufferMinus
        val maxCal = calorieGoal + bufferPlus

        val calories = ConsumedDailyList.getDailyCalories()
        var streak = prefs.getInt(KEY_STREAK, 0)

        if (calories in minCal..maxCal) {
            streak = (streak + 1).coerceAtMost(7) // cap at 7
        }

        prefs.edit()
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_LAST_UPDATE_DAY, today)
            .apply()
    }


    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_STREAK, 0)
    }

    fun resetWeeklyStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_STREAK, 0)
            .putInt(KEY_LAST_UPDATE_DAY, -1)
            .apply()
    }
}