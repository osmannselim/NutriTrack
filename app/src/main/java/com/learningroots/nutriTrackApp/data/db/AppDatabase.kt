package com.learningroots.nutriTrackApp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.learningroots.nutriTrackApp.data.dao.FoodIntakeDao
import com.learningroots.nutriTrackApp.data.dao.NutriCoachDao
import com.learningroots.nutriTrackApp.data.dao.PatientDao
import com.learningroots.nutriTrackApp.data.entity.FoodIntake
import com.learningroots.nutriTrackApp.data.entity.NutriCoachTip
import com.learningroots.nutriTrackApp.data.entity.Patient

@Database(
    entities = [Patient::class, FoodIntake::class, NutriCoachTip::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun foodIntakeDao(): FoodIntakeDao
    abstract fun nutriCoachDao(): NutriCoachDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutri_track_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
