package com.learningroots.nutriTrackApp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learningroots.nutriTrackApp.data.entity.NutriCoachTip

@Dao
interface NutriCoachDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tip: NutriCoachTip)

    @Query("SELECT * FROM nutricoach_tips WHERE patientId = :userId ORDER BY timestamp DESC")
    suspend fun getTipsForUser(userId: String): List<NutriCoachTip>
}
