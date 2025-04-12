package com.learningroots.nutriTrackApp.utils

import android.content.Context

object SharedPrefs {

    fun saveQuestionnaireData(
        userId: String,
        context: Context,
        foodSelections: Map<String, Boolean>,
        selectedPersona: String,
        biggestMealTime: String,
        sleepTime: String,
        wakeTime: String
    ) {
        val prefs = context.getSharedPreferences("QuestionnairePrefs_$userId", Context.MODE_PRIVATE) // Use user-specific name

        prefs.edit().apply {
            putString("persona", selectedPersona)
            putString("biggestMeal", biggestMealTime)
            putString("sleep", sleepTime)
            putString("wake", wakeTime)
            putStringSet("selectedFoods", foodSelections.filterValues { it }.keys)
            apply()
        }
    }

}
