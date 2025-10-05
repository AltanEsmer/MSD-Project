package com.medicationadherence.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.medicationadherence.app.data.local.converter.DateTimeConverters
import com.medicationadherence.app.data.local.converter.ListConverters
import kotlinx.datetime.LocalDateTime

/**
 * Room entity for Patient profile
 */
@Entity(tableName = "patients")
@TypeConverters(DateTimeConverters::class, ListConverters::class)
data class PatientEntity(
    @PrimaryKey val id: String,
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
 * Room entity for Medication
 */
@Entity(tableName = "medications")
@TypeConverters(DateTimeConverters::class, ListConverters::class)
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val frequency: List<String>, // JSON array of times
    val instructions: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * Room entity for Medication Schedule
 */
@Entity(tableName = "medication_schedules")
@TypeConverters(DateTimeConverters::class)
data class MedicationScheduleEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val scheduledTime: String, // ISO time format
    val date: String, // ISO date format
    val status: String, // AdherenceStatus enum as string
    val takenAt: LocalDateTime? = null,
    val skippedAt: LocalDateTime? = null
)

/**
 * Room entity for Adherence Records
 */
@Entity(tableName = "adherence_records")
@TypeConverters(DateTimeConverters::class)
data class AdherenceRecordEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val date: String, // ISO date format
    val status: String, // AdherenceStatus enum as string
    val timestamp: LocalDateTime? = null,
    val notes: String? = null
)

/**
 * Room entity for Medication Reminders
 */
@Entity(tableName = "medication_reminders")
@TypeConverters(DateTimeConverters::class)
data class MedicationReminderEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val scheduledTime: String, // ISO time format
    val isEnabled: Boolean = true,
    val snoozeCount: Int = 0,
    val lastSnoozeAt: LocalDateTime? = null
)
