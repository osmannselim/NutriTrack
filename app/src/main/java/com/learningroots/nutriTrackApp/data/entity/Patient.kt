package com.learningroots.nutriTrackApp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val userId: String,
    val phoneNumber: String,
    val name: String,
    val password: String,
    val sex: String,

    val HEIFAtotalscore: Float,
    val DiscretionaryHEIFAscore: Float,
    val VegetablesHEIFAscore: Float,
    val FruitHEIFAscore: Float,
)