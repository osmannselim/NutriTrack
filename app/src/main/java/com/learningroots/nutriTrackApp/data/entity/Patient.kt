package com.learningroots.nutriTrackApp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(

    val password: String? = null,
    val userName: String? = null,

    @PrimaryKey val userId: String,
    val phoneNumber: String?,
    val sex: String,

    val totalScore: Double,
    val discretionary:Double,
    val vegetables: Double,
    val fruits: Double,
    val grains: Double,
    val wholeGrains: Double,
    val meat: Double,
    val dairy: Double,
    val sodium: Double,
    val alcohol: Double,
    val water: Double,
    val sugar: Double,
    val saturatedFat: Double,
    val unsaturatedFat: Double
)