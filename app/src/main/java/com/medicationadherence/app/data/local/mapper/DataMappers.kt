package com.medicationadherence.app.data.local.mapper

import com.medicationadherence.app.data.local.entity.*
import com.medicationadherence.app.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/**
 * Mapper functions for converting between domain models and Room entities
 */

// Patient mappers
fun Patient.toEntity(): PatientEntity {
    return PatientEntity(
        id = id,
        name = name,
        email = email,
        age = age,
        conditions = conditions,
        emergencyContact = emergencyContact,
        shareDataEnabled = shareDataEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PatientEntity.toDomain(): Patient {
    return Patient(
        id = id,
        name = name,
        email = email,
        age = age,
        conditions = conditions,
        emergencyContact = emergencyContact,
        shareDataEnabled = shareDataEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Medication mappers
fun Medication.toEntity(): MedicationEntity {
    return MedicationEntity(
        id = id,
        name = name,
        dosage = dosage,
        frequency = frequency,
        instructions = instructions,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MedicationEntity.toDomain(): Medication {
    return Medication(
        id = id,
        name = name,
        dosage = dosage,
        frequency = frequency,
        instructions = instructions,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// MedicationSchedule mappers
fun MedicationSchedule.toEntity(): MedicationScheduleEntity {
    return MedicationScheduleEntity(
        id = id,
        medicationId = medicationId,
        scheduledTime = scheduledTime,
        date = date.toString(),
        status = status.name,
        takenAt = takenAt,
        skippedAt = skippedAt
    )
}

fun MedicationScheduleEntity.toDomain(): MedicationSchedule {
    return MedicationSchedule(
        id = id,
        medicationId = medicationId,
        scheduledTime = scheduledTime,
        date = LocalDate.parse(date),
        status = AdherenceStatus.valueOf(status),
        takenAt = takenAt,
        skippedAt = skippedAt
    )
}

// AdherenceRecord mappers
fun AdherenceRecord.toEntity(): AdherenceRecordEntity {
    return AdherenceRecordEntity(
        id = id,
        medicationId = medicationId,
        date = date.toString(),
        status = status.name,
        timestamp = timestamp,
        notes = notes
    )
}

fun AdherenceRecordEntity.toDomain(): AdherenceRecord {
    return AdherenceRecord(
        id = id,
        medicationId = medicationId,
        date = LocalDate.parse(date),
        status = AdherenceStatus.valueOf(status),
        timestamp = timestamp,
        notes = notes
    )
}

// MedicationReminder mappers
fun MedicationReminder.toEntity(): MedicationReminderEntity {
    return MedicationReminderEntity(
        id = id,
        medicationId = medicationId,
        scheduledTime = scheduledTime.toString(),
        isEnabled = isEnabled,
        snoozeCount = snoozeCount,
        lastSnoozeAt = lastSnoozeAt
    )
}

fun MedicationReminderEntity.toDomain(): MedicationReminder {
    return MedicationReminder(
        id = id,
        medicationId = medicationId,
        scheduledTime = LocalTime.parse(scheduledTime),
        isEnabled = isEnabled,
        snoozeCount = snoozeCount,
        lastSnoozeAt = lastSnoozeAt
    )
}

// Helper function to create MedicationWithSchedule
fun createMedicationWithSchedule(
    medication: Medication,
    schedules: List<MedicationSchedule>,
    adherenceRate: Float = 0f
): MedicationWithSchedule {
    return MedicationWithSchedule(
        medication = medication,
        schedules = schedules,
        adherenceRate = adherenceRate
    )
}
