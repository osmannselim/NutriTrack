package com.learningroots.nutriTrackApp.data.model

data class FruitData(
    val name: String,
    val family: String,
    val calories: Double,
    val fat: Double,
    val sugar: Double,
    val carbohydrates: Double,
    val protein: Double,
    val nutritions: Map<String, Double> // General purpose for other nutritions if any
) 