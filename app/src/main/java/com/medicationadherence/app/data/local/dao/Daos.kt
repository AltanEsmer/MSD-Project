package com.medicationadherence.app.data.local.dao

import androidx.room.*
import com.medicationadherence.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Patient operations
 */
@Dao
interface PatientDao {
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: String): PatientEntity?

    @Query("SELECT * FROM patients LIMIT 1")
    fun getCurrentPatient(): Flow<PatientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)
}

/**
 * DAO for Medication operations
 */
@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET isActive = 0 WHERE id = :id")
    suspend fun deleteMedication(id: String)

    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getActiveMedications(): Flow<List<MedicationEntity>>
}

/**
 * DAO for Medication Schedule operations
 */
@Dao
interface MedicationScheduleDao {
    @Query("SELECT * FROM medication_schedules WHERE date = :date ORDER BY scheduledTime ASC")
    fun getSchedulesForDate(date: String): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId AND date = :date")
    suspend fun getScheduleForMedicationAndDate(medicationId: String, date: String): MedicationScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationScheduleEntity)

    @Update
    suspend fun updateSchedule(schedule: MedicationScheduleEntity)

    @Query("UPDATE medication_schedules SET status = :status, takenAt = :takenAt WHERE id = :scheduleId")
    suspend fun updateScheduleStatus(scheduleId: String, status: String, takenAt: String?)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: String)

    @Query("SELECT * FROM medication_schedules WHERE date = :date AND status = 'PENDING'")
    fun getPendingSchedulesForDate(date: String): Flow<List<MedicationScheduleEntity>>
}

/**
 * DAO for Adherence Record operations
 */
@Dao
interface AdherenceRecordDao {
    @Query("SELECT * FROM adherence_records WHERE medicationId = :medicationId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getAdherenceHistory(medicationId: String, startDate: String, endDate: String): Flow<List<AdherenceRecordEntity>>

    @Query("SELECT * FROM adherence_records WHERE medicationId = :medicationId AND date = :date")
    suspend fun getAdherenceRecordForDate(medicationId: String, date: String): AdherenceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdherenceRecord(record: AdherenceRecordEntity)

    @Update
    suspend fun updateAdherenceRecord(record: AdherenceRecordEntity)

    @Query("SELECT COUNT(*) FROM adherence_records WHERE medicationId = :medicationId AND date BETWEEN :startDate AND :endDate AND status = 'TAKEN'")
    suspend fun getTakenCount(medicationId: String, startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM adherence_records WHERE medicationId = :medicationId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCount(medicationId: String, startDate: String, endDate: String): Int
}

/**
 * DAO for Medication Reminder operations
 */
@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders WHERE isEnabled = 1")
    fun getActiveReminders(): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE medicationId = :medicationId")
    suspend fun getReminderForMedication(medicationId: String): MedicationReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminderEntity)

    @Update
    suspend fun updateReminder(reminder: MedicationReminderEntity)

    @Query("UPDATE medication_reminders SET isEnabled = 0 WHERE medicationId = :medicationId")
    suspend fun disableReminder(medicationId: String)

    @Query("UPDATE medication_reminders SET snoozeCount = :count, lastSnoozeAt = :snoozeAt WHERE medicationId = :medicationId")
    suspend fun updateSnoozeInfo(medicationId: String, count: Int, snoozeAt: String?)
}
