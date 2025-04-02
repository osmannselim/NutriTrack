package com.learningroots.nutriTrackApp.utils

import android.content.Context

object SharedPrefs {

    private const val PREFS_NAME = "QuestionnairePrefs"

    fun saveQuestionnaireData(
        context: Context,
        foodSelections: Map<String, Boolean>,
        selectedPersona: String,
        biggestMealTime: String,
        sleepTime: String,
        wakeTime: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("persona", selectedPersona)
            putString("biggestMeal", biggestMealTime)
            putString("sleep", sleepTime)
            putString("wake", wakeTime)
            putStringSet("selectedFoods", foodSelections.filterValues { it }.keys)
            apply()
        }
    }

    fun getSelectedPersona(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("persona", "") ?: ""
}
