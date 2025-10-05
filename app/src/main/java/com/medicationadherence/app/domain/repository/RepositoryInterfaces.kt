package com.medicationadherence.app.domain.repository

import com.medicationadherence.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository interface for medication management
 */
interface MedicationRepository {
    // Medication CRUD operations
    fun getAllMedications(): Flow<List<Medication>>
    suspend fun getMedicationById(id: String): Medication?
    suspend fun insertMedication(medication: Medication): String
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(id: String)
    
    // Today's medications
    fun getTodayMedications(): Flow<List<MedicationWithSchedule>>
    fun getMedicationsForDate(date: LocalDate): Flow<List<MedicationWithSchedule>>
    
    // Medication schedules
    suspend fun getMedicationSchedules(date: LocalDate): Flow<List<MedicationSchedule>>
    suspend fun updateScheduleStatus(scheduleId: String, status: AdherenceStatus)
    
    // Adherence tracking
    suspend fun logDose(medicationId: String, status: AdherenceStatus, notes: String? = null)
    suspend fun getAdherenceHistory(medicationId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<AdherenceRecord>>
    suspend fun getAdherenceRate(medicationId: String, startDate: LocalDate, endDate: LocalDate): Flow<Float>
    
    // Sync operations
    suspend fun syncMedications()
    suspend fun syncAdherenceRecords()
}

/**
 * Repository interface for patient profile management
 */
interface PatientRepository {
    suspend fun getCurrentPatient(): Flow<Patient?>
    suspend fun updatePatient(patient: Patient)
    suspend fun createPatient(patient: Patient): String
    suspend fun syncPatientProfile()
}

/**
 * Repository interface for authentication
 */
interface AuthRepository {
    suspend fun signUp(email: String, password: String, userType: UserType): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signOut()
    suspend fun getCurrentUserId(): String?
    suspend fun isAuthenticated(): Boolean
    suspend fun linkPatientCaregiver(patientEmail: String, code: String): Result<Unit>
}

/**
 * Repository interface for medication reminders
 */
interface ReminderRepository {
    suspend fun scheduleReminder(medication: Medication, time: String)
    suspend fun cancelReminder(medicationId: String)
    suspend fun updateReminderStatus(medicationId: String, status: AdherenceStatus)
    suspend fun getActiveReminders(): Flow<List<MedicationReminder>>
    suspend fun snoozeReminder(medicationId: String, minutes: Int = 15)
}
