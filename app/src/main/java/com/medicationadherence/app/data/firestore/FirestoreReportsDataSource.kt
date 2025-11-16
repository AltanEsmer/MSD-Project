package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.medicationadherence.app.domain.model.*
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Firestore data source for report generation
 */
@Singleton
class FirestoreReportsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val patientDataSource: FirestorePatientDataSource,
    private val medicationDataSource: FirestoreMedicationDataSource,
    private val adherenceDataSource: FirestoreAdherenceDataSource
) {
    /**
     * Generate adherence report for a patient within a date range
     */
    suspend fun getAdherenceReport(
        patientId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): AdherenceReport {
        try {
            val patient = patientDataSource.getPatient(patientId)
            val patientName = patient?.name ?: "Unknown Patient"
            
            // Get all adherence records for the period
            val medications = medicationDataSource.getMedications(patientId)
            val allRecords = mutableListOf<AdherenceRecord>()
            
            medications.forEach { medication ->
                val records = adherenceDataSource.getAdherenceRecords(
                    patientId, medication.id, startDate, endDate
                )
                allRecords.addAll(records)
            }

            // Calculate overall stats
            val totalDoses = allRecords.size
            val takenDoses = allRecords.count { it.status == AdherenceStatus.TAKEN }
            val missedDoses = allRecords.count { it.status == AdherenceStatus.MISSED }
            val skippedDoses = allRecords.count { it.status == AdherenceStatus.SKIPPED }
            val overallAdherenceRate = if (totalDoses > 0) {
                (takenDoses.toFloat() / totalDoses) * 100
            } else {
                0f
            }

            // Calculate streaks
            val sortedRecords = allRecords.sortedBy { it.date }
            val streaks = calculateStreaks(sortedRecords)

            // Generate weekly data
            val weeklyData = generateWeeklyData(allRecords, startDate, endDate)

            // Generate medication breakdown
            val medicationBreakdown = generateMedicationBreakdown(medications, allRecords)

            // Generate insights
            val insights = generateInsights(overallAdherenceRate, missedDoses, streaks.first, medications.size)

            return AdherenceReport(
                patientId = patientId,
                patientName = patientName,
                startDate = startDate,
                endDate = endDate,
                overallAdherenceRate = overallAdherenceRate,
                totalDoses = totalDoses,
                takenDoses = takenDoses,
                missedDoses = missedDoses,
                skippedDoses = skippedDoses,
                currentStreak = streaks.first,
                bestStreak = streaks.second,
                weeklyData = weeklyData,
                medicationBreakdown = medicationBreakdown,
                insights = insights
            )
        } catch (e: Exception) {
            // Return empty report on error
            return AdherenceReport(
                patientId = patientId,
                patientName = "Unknown Patient",
                startDate = startDate,
                endDate = endDate,
                overallAdherenceRate = 0f,
                totalDoses = 0,
                takenDoses = 0,
                missedDoses = 0,
                skippedDoses = 0,
                currentStreak = 0,
                bestStreak = 0,
                weeklyData = emptyList(),
                medicationBreakdown = emptyList(),
                insights = listOf("Unable to generate report. Please try again.")
            )
        }
    }

    /**
     * Get overall summary stats for all patients with data sharing enabled
     */
    suspend fun getOverallSummaryStats(): OverallSummaryStats {
        return try {
            val patients = patientDataSource.getAllPatientsWithSharingEnabled()
            var totalMedications = 0
            var totalDoses = 0
            var totalTaken = 0
            var totalMissed = 0

            patients.forEach { patient ->
                val medications = medicationDataSource.getMedications(patient.id)
                totalMedications += medications.size

                // Get last 7 days of data
                val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startDate = endDate.minus(7, DateTimeUnit.DAY)

                medications.forEach { medication ->
                    val records = adherenceDataSource.getAdherenceRecords(
                        patient.id, medication.id, startDate, endDate
                    )
                    totalDoses += records.size
                    totalTaken += records.count { it.status == AdherenceStatus.TAKEN }
                    totalMissed += records.count { it.status == AdherenceStatus.MISSED }
                }
            }

            val overallAdherence = if (totalDoses > 0) {
                (totalTaken.toFloat() / totalDoses) * 100
            } else {
                0f
            }

            OverallSummaryStats(
                overallAdherence = overallAdherence,
                totalMedications = totalMedications,
                totalMissedDoses = totalMissed,
                activePatients = patients.size
            )
        } catch (e: Exception) {
            OverallSummaryStats(
                overallAdherence = 0f,
                totalMedications = 0,
                totalMissedDoses = 0,
                activePatients = 0
            )
        }
    }

    /**
     * Get adherence reports for multiple patients
     */
    suspend fun getMultiplePatientReports(
        patientIds: List<String>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AdherenceReport> {
        return patientIds.map { patientId ->
            getAdherenceReport(patientId, startDate, endDate)
        }
    }

    /**
     * Calculate current and best streak
     */
    private fun calculateStreaks(sortedRecords: List<AdherenceRecord>): Pair<Int, Int> {
        if (sortedRecords.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0
        var previousDate: LocalDate? = null

        sortedRecords.forEach { record ->
            if (record.status == AdherenceStatus.TAKEN) {
                if (previousDate == null) {
                    tempStreak = 1
                } else {
                    val daysDiff = record.date.toEpochDays() - previousDate!!.toEpochDays()
                    if (daysDiff == 1) {
                        tempStreak++
                    } else {
                        tempStreak = 1
                    }
                }
                bestStreak = maxOf(bestStreak, tempStreak)
            } else {
                tempStreak = 0
            }
            previousDate = record.date
        }

        // Current streak is the last consecutive taken streak
        if (sortedRecords.lastOrNull()?.status == AdherenceStatus.TAKEN) {
            var i = sortedRecords.size - 1
            while (i >= 0 && sortedRecords[i].status == AdherenceStatus.TAKEN) {
                currentStreak++
                i--
                if (i >= 0) {
                    val daysDiff = sortedRecords[i + 1].date.toEpochDays() - sortedRecords[i].date.toEpochDays()
                    if (daysDiff != 1) break
                }
            }
        }

        return Pair(currentStreak, bestStreak)
    }

    /**
     * Generate weekly adherence data
     */
    private fun generateWeeklyData(
        records: List<AdherenceRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WeeklyAdherenceData> {
        val weeklyData = mutableListOf<WeeklyAdherenceData>()
        var currentDate = startDate

        while (currentDate <= endDate) {
            val weekEnd = currentDate.plus(6, DateTimeUnit.DAY)
            val weekRecords = records.filter { it.date >= currentDate && it.date <= weekEnd }
            
            val totalDoses = weekRecords.size
            val takenDoses = weekRecords.count { it.status == AdherenceStatus.TAKEN }
            val adherenceRate = if (totalDoses > 0) {
                (takenDoses.toFloat() / totalDoses) * 100
            } else {
                0f
            }

            weeklyData.add(
                WeeklyAdherenceData(
                    weekStartDate = currentDate,
                    adherenceRate = adherenceRate,
                    takenDoses = takenDoses,
                    totalDoses = totalDoses
                )
            )

            currentDate = currentDate.plus(7, DateTimeUnit.DAY)
        }

        return weeklyData
    }

    /**
     * Generate medication-specific adherence breakdown
     */
    private fun generateMedicationBreakdown(
        medications: List<Medication>,
        records: List<AdherenceRecord>
    ): List<MedicationAdherence> {
        return medications.map { medication ->
            val medRecords = records.filter { it.medicationId == medication.id }
            val totalDoses = medRecords.size
            val takenDoses = medRecords.count { it.status == AdherenceStatus.TAKEN }
            val missedDoses = medRecords.count { it.status == AdherenceStatus.MISSED }
            val adherenceRate = if (totalDoses > 0) {
                (takenDoses.toFloat() / totalDoses) * 100
            } else {
                0f
            }

            MedicationAdherence(
                medicationId = medication.id,
                medicationName = medication.name,
                adherenceRate = adherenceRate,
                takenDoses = takenDoses,
                totalDoses = totalDoses,
                missedDoses = missedDoses
            )
        }
    }

    /**
     * Generate insights based on adherence data
     */
    private fun generateInsights(
        adherenceRate: Float,
        missedDoses: Int,
        currentStreak: Int,
        medicationCount: Int
    ): List<String> {
        val insights = mutableListOf<String>()

        when {
            adherenceRate >= 90 -> insights.add("Excellent adherence! Keep up the great work.")
            adherenceRate >= 75 -> insights.add("Good adherence. A few improvements can make it perfect.")
            adherenceRate >= 50 -> insights.add("Adherence needs attention. Consider setting more reminders.")
            else -> insights.add("Low adherence detected. Please reach out for support.")
        }

        if (currentStreak >= 7) {
            insights.add("Amazing $currentStreak-day streak! Consistency is key.")
        } else if (currentStreak >= 3) {
            insights.add("Building momentum with a $currentStreak-day streak.")
        }

        if (missedDoses > 5) {
            insights.add("$missedDoses missed doses this period. Let's work on reducing this.")
        }

        if (medicationCount > 5) {
            insights.add("Managing $medicationCount medications. Consider using reminders for each.")
        }

        if (insights.isEmpty()) {
            insights.add("Continue monitoring adherence patterns.")
        }

        return insights
    }
}
