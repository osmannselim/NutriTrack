package com.learningroots.nutriTrackApp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutricoach_tips",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["userId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class NutriCoachTip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
