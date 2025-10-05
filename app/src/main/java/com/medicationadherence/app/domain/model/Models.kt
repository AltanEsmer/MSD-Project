package com.medicationadherence.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/**
 * Core domain model for Patient profile
 */
data class Patient(
    val id: String = "",
    val name: String,
    val email: String,
    val age: Int,
    val conditions: List<String>,
    val emergencyContact: String,
    val shareDataEnabled: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * Core domain model for Medication
 */
data class Medication(
    val id: String = "",
    val name: String,
    val dosage: String,
    val frequency: List<String>, // ["08:00", "20:00"] format
    val instructions: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * Medication schedule for specific time slots
 */
data class MedicationSchedule(
    val id: String = "",
    val medicationId: String,
    val scheduledTime: String, // ISO time format
    val date: LocalDate,
    val status: AdherenceStatus = AdherenceStatus.PENDING,
    val takenAt: LocalDateTime? = null,
    val skippedAt: LocalDateTime? = null
)

/**
 * Adherence status enumeration
 */
enum class AdherenceStatus {
    TAKEN,
    MISSED,
    SKIPPED,
    PENDING
}

/**
 * Adherence record for tracking medication compliance
 */
data class AdherenceRecord(
    val id: String = "",
    val medicationId: String,
    val date: LocalDate,
    val status: AdherenceStatus,
    val timestamp: LocalDateTime? = null,
    val notes: String? = null
)

/**
 * Medication reminder data
 */
data class MedicationReminder(
    val id: String = "",
    val medicationId: String,
    val scheduledTime: LocalTime,
    val isEnabled: Boolean = true,
    val snoozeCount: Int = 0,
    val lastSnoozeAt: LocalDateTime? = null
)

/**
 * Today's medication with schedule information
 */
data class MedicationWithSchedule(
    val medication: Medication,
    val schedules: List<MedicationSchedule>,
    val adherenceRate: Float = 0f
)

/**
 * User authentication state
 */
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * User type for authentication
 */
enum class UserType {
    PATIENT,
    FAMILY_MEMBER
}
