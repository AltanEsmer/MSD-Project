package com.medicationadherence.app.data.firestore.mapper

import com.medicationadherence.app.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Mapper functions for converting between domain models and Firestore maps
 */

// Patient mappers
fun Patient.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "age" to age,
        "conditions" to conditions,
        "emergencyContact" to emergencyContact,
        "bloodType" to bloodType,
        "shareDataEnabled" to shareDataEnabled,
        "createdAt" to (createdAt?.toFirestoreTimestamp() ?: Timestamp.now()),
        "updatedAt" to (updatedAt?.toFirestoreTimestamp() ?: Timestamp.now())
    )
}

fun Map<String, Any?>.toPatient(): Patient {
    return Patient(
        id = (this["id"] as? String) ?: "",
        name = (this["name"] as? String) ?: "",
        email = (this["email"] as? String) ?: "",
        age = (this["age"] as? Long)?.toInt() ?: (this["age"] as? Int) ?: 0,
        conditions = (this["conditions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        emergencyContact = (this["emergencyContact"] as? String) ?: "",
        bloodType = this["bloodType"] as? String,
        shareDataEnabled = (this["shareDataEnabled"] as? Boolean) ?: true,
        createdAt = (this["createdAt"] as? Timestamp)?.toLocalDateTime(),
        updatedAt = (this["updatedAt"] as? Timestamp)?.toLocalDateTime()
    )
}

// Medication mappers
fun Medication.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "dosage" to dosage,
        "frequency" to frequency,
        "instructions" to instructions,
        "isActive" to isActive,
        "createdAt" to (createdAt?.toFirestoreTimestamp() ?: Timestamp.now()),
        "updatedAt" to (updatedAt?.toFirestoreTimestamp() ?: Timestamp.now())
    )
}

fun Map<String, Any?>.toMedication(): Medication {
    return Medication(
        id = (this["id"] as? String) ?: "",
        name = (this["name"] as? String) ?: "",
        dosage = (this["dosage"] as? String) ?: "",
        frequency = (this["frequency"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        instructions = (this["instructions"] as? String) ?: "",
        isActive = (this["isActive"] as? Boolean) ?: true,
        createdAt = (this["createdAt"] as? Timestamp)?.toLocalDateTime(),
        updatedAt = (this["updatedAt"] as? Timestamp)?.toLocalDateTime()
    )
}

// AdherenceRecord mappers
fun AdherenceRecord.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "medicationId" to medicationId,
        "date" to date.toString(),
        "status" to status.name,
        "timestamp" to (timestamp?.toFirestoreTimestamp()),
        "notes" to notes
    )
}

fun Map<String, Any?>.toAdherenceRecord(): AdherenceRecord {
    return AdherenceRecord(
        id = (this["id"] as? String) ?: "",
        medicationId = (this["medicationId"] as? String) ?: "",
        date = LocalDate.parse((this["date"] as? String) ?: LocalDate(2024, 1, 1).toString()),
        status = AdherenceStatus.valueOf((this["status"] as? String) ?: "PENDING"),
        timestamp = (this["timestamp"] as? Timestamp)?.toLocalDateTime(),
        notes = this["notes"] as? String
    )
}

// Extension functions for timestamp conversion
fun LocalDateTime.toFirestoreTimestamp(): Timestamp {
    val javaLocalDateTime = java.time.LocalDateTime.of(
        this.year,
        this.monthNumber,
        this.dayOfMonth,
        this.hour,
        this.minute,
        this.second
    )
    val instant = javaLocalDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
    val date = Date.from(instant)
    return Timestamp(date)
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    val date = this.toDate()
    val instant = date.toInstant()
    val javaLocalDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    return LocalDateTime(
        year = javaLocalDateTime.year,
        monthNumber = javaLocalDateTime.monthValue,
        dayOfMonth = javaLocalDateTime.dayOfMonth,
        hour = javaLocalDateTime.hour,
        minute = javaLocalDateTime.minute,
        second = javaLocalDateTime.second
    )
}

