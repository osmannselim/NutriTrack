package com.learningroots.nutriTrackApp.data.model

data class FruityViceNutritions(
    val calories: Double? = null, // Changed to Double to match FruitData, API might return Int or Double
    val fat: Double? = null,
    val sugar: Double? = null,
    val carbohydrates: Double? = null,
    val protein: Double? = null
) 