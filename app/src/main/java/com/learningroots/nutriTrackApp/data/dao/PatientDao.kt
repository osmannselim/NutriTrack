package com.learningroots.nutriTrackApp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.learningroots.nutriTrackApp.data.entity.Patient

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    @Query("SELECT * FROM patients WHERE userId = :userId")
    suspend fun getPatientById(userId: String): Patient?

    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>

    @Query("UPDATE patients SET userName = :newName WHERE userId = :userId")
    suspend fun updateName(userId: String, newName: String)

//    @Query("UPDATE patients SET password = :newPass WHERE userId = :userId")
//    suspend fun updatePassword(userId: String, newPass: String)


    @Update
    suspend fun update(patient: Patient)
}
