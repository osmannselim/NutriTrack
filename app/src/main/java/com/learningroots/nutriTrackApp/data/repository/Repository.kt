package com.learningroots.nutriTrackApp.data.repository

import com.learningroots.nutriTrackApp.data.dao.FoodIntakeDao
import com.learningroots.nutriTrackApp.data.dao.PatientDao
import com.learningroots.nutriTrackApp.data.dao.NutriCoachDao
import com.learningroots.nutriTrackApp.data.entity.*

class Repository(
    private val patientDao: PatientDao,
    private val foodIntakeDao: FoodIntakeDao,
    private val nutriCoachDao: NutriCoachDao
) {
    suspend fun insertPatient(patient: Patient) = patientDao.insertAll(listOf(patient))
    suspend fun getPatientById(userId: String) = patientDao.getPatientById(userId)

    suspend fun saveFoodIntake(foodIntake: FoodIntake) = foodIntakeDao.insert(foodIntake)
    suspend fun getFoodIntake(userId: String) = foodIntakeDao.getFoodIntakeByUser(userId)

    suspend fun getTips(userId: String) = nutriCoachDao.getTipsForUser(userId)
    suspend fun saveTip(tip: NutriCoachTip) = nutriCoachDao.insert(tip)

    suspend fun updatePatient(patient: Patient) = patientDao.update(patient)

    suspend fun getAllPatients(): List<Patient> {
        return patientDao.getAllPatients()
    }
}