package com.learningroots.nutriTrackApp.data.mapper

import com.learningroots.nutriTrackApp.data.entity.FoodIntake

object FoodIntakeMapper {
    fun fromEntity(entity: FoodIntake): FoodIntakeUIModel {
        return FoodIntakeUIModel(
            persona = entity.persona,
            biggestMealTime = entity.biggestMealTime,
            sleepTime = entity.sleepTime,
            wakeTime = entity.wakeTime,
            selectedFoods = entity.selectedFoods.split(",").toSet()
        )
    }

    fun toEntity(
        patientId: String,
        persona: String,
        biggestMealTime: String,
        sleepTime: String,
        wakeTime: String,
        selectedFoods: Set<String>
    ): FoodIntake {
        return FoodIntake(
            patientId = patientId,
            persona = persona,
            biggestMealTime = biggestMealTime,
            sleepTime = sleepTime,
            wakeTime = wakeTime,
            selectedFoods = selectedFoods.joinToString(",")
        )
    }
}

data class FoodIntakeUIModel(
    val persona: String,
    val biggestMealTime: String,
    val sleepTime: String,
    val wakeTime: String,
    val selectedFoods: Set<String>
)