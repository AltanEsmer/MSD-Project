package com.medicationadherence.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.medicationadherence.app.data.firestore.FirestoreAdherenceDataSource
import com.medicationadherence.app.data.firestore.FirestoreMedicationDataSource
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.work.MedicationReminderManager
import com.medicationadherence.app.domain.model.*
import com.medicationadherence.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for medication management
 */
@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMedicationDataSource,
    private val firestoreMedicationDataSource: FirestoreMedicationDataSource,
    private val firestoreAdherenceDataSource: FirestoreAdherenceDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val reminderManager: MedicationReminderManager
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> {
        return localDataSource.getAllMedications()
    }

    override suspend fun getMedicationById(id: String): Medication? {
        return localDataSource.getMedicationById(id)
    }

    override suspend fun insertMedication(medication: Medication): String {
        val id = localDataSource.insertMedication(medication)
        
        // Schedule reminders for the new medication
        val medicationWithId = medication.copy(id = id)
        reminderManager.scheduleReminder(id)
        
        // Sync to Firestore in background (don't block on it)
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                val updatedMedication = medication.copy(id = id)
                firestoreMedicationDataSource.saveMedication(userId, updatedMedication)
            } catch (e: Exception) {
                // Log error but don't throw - local save succeeded
                println("Failed to sync medication to Firestore: ${e.message}")
            }
        }
        
        return id
    }

    override suspend fun updateMedication(medication: Medication) {
        localDataSource.updateMedication(medication)
        
        // Sync to Firestore
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                firestoreMedicationDataSource.saveMedication(userId, medication)
            } catch (e: Exception) {
                println("Failed to sync medication update to Firestore: ${e.message}")
            }
        }
    }

    override suspend fun deleteMedication(id: String) {
        localDataSource.deleteMedication(id)
        
        // Cancel reminders for deleted medication
        reminderManager.cancelReminder(id)
        
        // Sync to Firestore
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                firestoreMedicationDataSource.deleteMedication(userId, id)
            } catch (e: Exception) {
                println("Failed to sync medication deletion to Firestore: ${e.message}")
            }
        }
    }

    override fun getTodayMedications(): Flow<List<MedicationWithSchedule>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getMedicationsForDate(today)
    }

    override fun getMedicationsForDate(date: LocalDate): Flow<List<MedicationWithSchedule>> {
        return localDataSource.getMedicationSchedules(date)
            .map { scheduleList ->
                val medicationIds = scheduleList.map { it.medicationId }.distinct()
                val medications = runBlocking {
                    medicationIds.mapNotNull { id ->
                        localDataSource.getMedicationById(id)
                    }
                }
                
                medications.map { medication ->
                    val medicationSchedules = scheduleList.filter { it.medicationId == medication.id }
                    val adherenceRate = runBlocking { calculateAdherenceRate(medication.id, date) }
                    
                    MedicationWithSchedule(
                        medication = medication,
                        schedules = medicationSchedules,
                        adherenceRate = adherenceRate
                    )
                }
            }
    }

    override fun getMedicationSchedules(date: LocalDate): Flow<List<MedicationSchedule>> {
        return localDataSource.getMedicationSchedules(date)
    }

    override suspend fun updateScheduleStatus(scheduleId: String, status: AdherenceStatus) {
        localDataSource.updateScheduleStatus(scheduleId, status)
        
        // If medication is marked as taken, auto-reschedule next reminder
        if (status == AdherenceStatus.TAKEN) {
            // Find the schedule to get medication ID and scheduled time
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val schedules = localDataSource.getMedicationSchedules(today).first()
            val schedule = schedules.firstOrNull { it.id == scheduleId }
            
            if (schedule != null) {
                val medication = localDataSource.getMedicationById(schedule.medicationId)
                if (medication != null) {
                    // Auto-reschedule next reminder
                    reminderManager.scheduleNextReminder(medication, schedule.scheduledTime)
                }
            }
        }
    }

    override suspend fun logDose(medicationId: String, status: AdherenceStatus, notes: String?) {
        localDataSource.logDose(medicationId, status, notes)
        
        // Sync to Firestore
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val today = now.date
                val timestamp = now
                
                val record = AdherenceRecord(
                    id = java.util.UUID.randomUUID().toString(),
                    medicationId = medicationId,
                    date = today,
                    status = status,
                    timestamp = timestamp,
                    notes = notes
                )
                
                firestoreAdherenceDataSource.saveAdherenceRecord(userId, record)
            } catch (e: Exception) {
                println("Failed to sync adherence record to Firestore: ${e.message}")
            }
        }
    }

    override suspend fun getAdherenceHistory(
        medicationId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<AdherenceRecord>> {
        return localDataSource.getAdherenceHistory(medicationId, startDate, endDate)
    }

    override suspend fun getAdherenceRate(
        medicationId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Float> {
        val rate = localDataSource.getAdherenceRate(medicationId, startDate, endDate)
        return kotlinx.coroutines.flow.flowOf(rate)
    }

    override suspend fun syncMedications() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        try {
            // Fetch from Firestore
            val firestoreMedications = firestoreMedicationDataSource.syncAllMedications(userId)
            
            // Save to local database
            firestoreMedications.forEach { medication ->
                try {
                    localDataSource.insertMedication(medication)
                } catch (e: Exception) {
                    println("Failed to save medication to local DB: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Failed to sync medications from Firestore: ${e.message}")
        }
    }

    override suspend fun syncAdherenceRecords() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        try {
            // Get all local medications
            val medications = localDataSource.getAllMedications().let { flow ->
                runBlocking {
                    var result: List<Medication> = emptyList()
                    flow.collect { result = it }
                    result
                }
            }
            
            // For each medication, get adherence records from local DB and sync to Firestore
            medications.forEach { medication ->
                val startDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    .let { date ->
                        val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
                            .minusDays(30)
                        LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
                    }
                val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                
                val records = localDataSource.getAdherenceHistory(
                    medication.id,
                    startDate,
                    endDate
                ).let { flow ->
                    runBlocking {
                        var result: List<AdherenceRecord> = emptyList()
                        flow.collect { result = it }
                        result
                    }
                }
                
                if (records.isNotEmpty()) {
                    firestoreAdherenceDataSource.saveAdherenceRecords(userId, records)
                }
            }
        } catch (e: Exception) {
            println("Failed to sync adherence records to Firestore: ${e.message}")
        }
    }

    private suspend fun calculateAdherenceRate(medicationId: String, date: LocalDate): Float {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Use java.time for proper date arithmetic
        val javaCurrentDate = java.time.LocalDate.of(currentDate.year, currentDate.monthNumber, currentDate.dayOfMonth)
        val javaStartDate = javaCurrentDate.minusDays(30)
        val startDate = LocalDate(javaStartDate.year, javaStartDate.monthValue, javaStartDate.dayOfMonth)
        return localDataSource.getAdherenceRate(medicationId, startDate, date)
    }
}
