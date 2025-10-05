package com.medicationadherence.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.medicationadherence.app.data.local.converter.DateTimeConverters
import com.medicationadherence.app.data.local.converter.ListConverters
import com.medicationadherence.app.data.local.dao.*
import com.medicationadherence.app.data.local.entity.*

/**
 * Room database for Medication Adherence App
 */
@Database(
    entities = [
        PatientEntity::class,
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        AdherenceRecordEntity::class,
        MedicationReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class, ListConverters::class)
abstract class MedicationDatabase : RoomDatabase() {
    
    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun adherenceRecordDao(): AdherenceRecordDao
    abstract fun medicationReminderDao(): MedicationReminderDao

    companion object {
        @Volatile
        private var INSTANCE: MedicationDatabase? = null

        fun getDatabase(context: Context): MedicationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicationDatabase::class.java,
                    "medication_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
