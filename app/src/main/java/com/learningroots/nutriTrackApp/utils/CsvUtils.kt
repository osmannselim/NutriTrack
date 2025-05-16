package com.learningroots.nutriTrackApp.utils

import com.learningroots.nutriTrackApp.data.entity.Patient

import android.content.Context

fun loadPatientsFromCSV(context: Context): List<Patient> {
    val inputStream = context.assets.open("users.csv")
    val reader = inputStream.bufferedReader()

    val patients = mutableListOf<Patient>()
    reader.useLines { lines ->
        lines.drop(1).forEach { line ->
            val tokens = line.split(",")
            if (tokens.size >= 3) {

                val isMale = tokens[2].trim().equals("male", ignoreCase = true)

                val get = { maleIndex: Int, femaleIndex: Int ->
                    tokens[if (isMale) maleIndex else femaleIndex].toDoubleOrNull() ?: 0.0
                }

                patients.add(
                    Patient(
                        password = null,
                        userName = null,

                        userId = tokens[1].trim(),
                        phoneNumber = tokens[0].trim(),
                        sex = tokens[2].trim(),

                        totalScore = get(3, 4),
                        discretionary = get(5, 6),
                        vegetables = get(8, 9),
                        fruits = get(19, 20),
                        grains = get(29, 30),  // grains and cereals
                        wholeGrains = get(33, 34),
                        meat = get(36, 37),  // meat and alternatives
                        dairy = get(40, 41),  // diary and alternatives
                        sodium = get(43, 44),
                        alcohol = get(46, 47),
                        water = get(49, 50),
                        sugar = get(54, 55),
                        saturatedFat = get(57,58),
                        unsaturatedFat = get(60, 61)
                    )
                )
            }
        }
    }
    return patients
}
