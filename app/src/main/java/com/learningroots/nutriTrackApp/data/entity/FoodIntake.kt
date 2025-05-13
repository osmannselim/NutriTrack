package com.learningroots.nutriTrackApp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_intake",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["userId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FoodIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val persona: String,
    val biggestMealTime: String,
    val sleepTime: String,
    val wakeTime: String,
    val selectedFoods: String // Comma-separated string (or change to List<String> with TypeConverter)
)