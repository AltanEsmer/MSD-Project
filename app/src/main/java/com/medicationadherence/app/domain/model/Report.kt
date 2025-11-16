package com.medicationadherence.app.domain.model

import kotlinx.datetime.LocalDate

/**
 * Domain model for adherence report
 */
data class AdherenceReport(
    val patientId: String,
    val patientName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val overallAdherenceRate: Float,
    val totalDoses: Int,
    val takenDoses: Int,
    val missedDoses: Int,
    val skippedDoses: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val weeklyData: List<WeeklyAdherenceData>,
    val medicationBreakdown: List<MedicationAdherence>,
    val insights: List<String>
)

/**
 * Weekly adherence data point
 */
data class WeeklyAdherenceData(
    val weekStartDate: LocalDate,
    val adherenceRate: Float,
    val takenDoses: Int,
    val totalDoses: Int
)

/**
 * Adherence data for a specific medication
 */
data class MedicationAdherence(
    val medicationId: String,
    val medicationName: String,
    val adherenceRate: Float,
    val takenDoses: Int,
    val totalDoses: Int,
    val missedDoses: Int
)

/**
 * Time of day adherence pattern
 */
data class TimeOfDayAdherence(
    val hour: Int,
    val adherenceRate: Float,
    val totalDoses: Int
)

/**
 * Summary statistics for all patients
 */
data class OverallSummaryStats(
    val overallAdherence: Float,
    val totalMedications: Int,
    val totalMissedDoses: Int,
    val activePatients: Int
)
