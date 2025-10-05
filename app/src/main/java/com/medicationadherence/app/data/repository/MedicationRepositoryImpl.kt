package com.medicationadherence.app.data.repository

import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.domain.model.*
import com.medicationadherence.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for medication management
 */
@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMedicationDataSource
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> {
        return localDataSource.getAllMedications()
    }

    override suspend fun getMedicationById(id: String): Medication? {
        return localDataSource.getMedicationById(id)
    }

    override suspend fun insertMedication(medication: Medication): String {
        return localDataSource.insertMedication(medication)
    }

    override suspend fun updateMedication(medication: Medication) {
        localDataSource.updateMedication(medication)
    }

    override suspend fun deleteMedication(id: String) {
        localDataSource.deleteMedication(id)
    }

    override fun getTodayMedications(): Flow<List<MedicationWithSchedule>> {
        val today = kotlinx.datetime.LocalDate(2024, 1, 1) // Temporary fix
        return getMedicationsForDate(today)
    }

    override fun getMedicationsForDate(date: LocalDate): Flow<List<MedicationWithSchedule>> {
        return kotlinx.coroutines.flow.flow {
            val schedules = localDataSource.getMedicationSchedules(date)
            schedules.collect { scheduleList ->
                val medicationIds = scheduleList.map { it.medicationId }.distinct()
                val medications = medicationIds.mapNotNull { id ->
                    localDataSource.getMedicationById(id)
                }
                
                val result = medications.map { medication ->
                    val medicationSchedules = scheduleList.filter { it.medicationId == medication.id }
                    val adherenceRate = calculateAdherenceRate(medication.id, date)
                    
                    MedicationWithSchedule(
                        medication = medication,
                        schedules = medicationSchedules,
                        adherenceRate = adherenceRate
                    )
                }
                emit(result)
            }
        }
    }

    override suspend fun getMedicationSchedules(date: LocalDate): Flow<List<MedicationSchedule>> {
        return localDataSource.getMedicationSchedules(date)
    }

    override suspend fun updateScheduleStatus(scheduleId: String, status: AdherenceStatus) {
        localDataSource.updateScheduleStatus(scheduleId, status)
    }

    override suspend fun logDose(medicationId: String, status: AdherenceStatus, notes: String?) {
        localDataSource.logDose(medicationId, status, notes)
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
        // TODO: Implement Firebase sync
        // This will be implemented when we add Firebase integration
    }

    override suspend fun syncAdherenceRecords() {
        // TODO: Implement Firebase sync
        // This will be implemented when we add Firebase integration
    }

    private suspend fun calculateAdherenceRate(medicationId: String, date: LocalDate): Float {
        val startDate = kotlinx.datetime.LocalDate(2024, 1, 1) // Temporary fix
        return localDataSource.getAdherenceRate(medicationId, startDate, date)
    }
}
