package com.example.caloriecounternew

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

object CalorieStreakManager {
    private const val PREFS_NAME = "CalorieStreakPrefs"
    private const val KEY_STREAK = "streak_count"
    private const val KEY_LAST_UPDATE_DAY = "last_update_day"

    private const val MIN_CAL = 1700
    private const val MAX_CAL = 2300

    fun checkAndUpdateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastUpdateDay = prefs.getInt(KEY_LAST_UPDATE_DAY, -1)

        //if (today != lastUpdateDay) {
        val calories = ConsumedDailyList.getDailyCalories()
        var streak = prefs.getInt(KEY_STREAK, 0)

        if (calories in MIN_CAL..MAX_CAL) {
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
