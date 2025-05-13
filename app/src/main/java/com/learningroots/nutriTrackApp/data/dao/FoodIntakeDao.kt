package com.learningroots.nutriTrackApp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learningroots.nutriTrackApp.data.entity.FoodIntake

@Dao
interface FoodIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodIntake: FoodIntake)

    @Query("SELECT * FROM food_intake WHERE patientId = :userId")
    suspend fun getFoodIntakeByUser(userId: String): FoodIntake?
}