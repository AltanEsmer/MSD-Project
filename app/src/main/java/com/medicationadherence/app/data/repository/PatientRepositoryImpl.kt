package com.medicationadherence.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.local.dao.PatientDao
import com.medicationadherence.app.data.local.entity.PatientEntity
import com.medicationadherence.app.data.local.mapper.toDomain
import com.medicationadherence.app.data.local.mapper.toEntity
import com.medicationadherence.app.domain.model.Patient
import com.medicationadherence.app.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Patient repository implementation with Firestore and Room sync
 */
@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val firestorePatientDataSource: FirestorePatientDataSource,
    private val patientDao: PatientDao,
    private val firebaseAuth: FirebaseAuth
) : PatientRepository {

    /**
     * Get current patient profile
     * Priority: Firestore -> Room -> null
     */
    override suspend fun getCurrentPatient(): Flow<Patient?> {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            return flowOf(null)
        }

        return patientDao.getCurrentPatient(userId).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Create patient profile (saves to both Firestore and Room)
     * Gracefully handles Firestore failures - saves to Room even if Firestore fails
     */
    override suspend fun createPatient(patient: Patient): String {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to create patient profile")

        val patientWithId = if (patient.id.isEmpty()) {
            patient.copy(id = userId)
        } else {
            patient
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val patientWithTimestamps = patientWithId.copy(
            createdAt = now,
            updatedAt = now
        )

        // Always save to Room first (for offline-first approach)
        patientDao.insertPatient(patientWithTimestamps.toEntity())

        // Try to sync to Firestore (don't throw if it fails - data is already in Room)
        val firestoreResult = firestorePatientDataSource.savePatient(patientWithTimestamps)
        firestoreResult.onFailure { e ->
            // Log error but don't throw - local save succeeded
            println("Failed to sync patient profile to Firestore: ${e.message}")
        }

        return patientWithTimestamps.id
    }

    /**
     * Update patient profile (syncs to both Firestore and Room)
     * Gracefully handles Firestore failures - saves to Room even if Firestore fails
     */
    override suspend fun updatePatient(patient: Patient) {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to update patient profile")

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val patientWithTimestamp = patient.copy(
            id = userId,
            updatedAt = now
        )

        // Always save to Room first (for offline-first approach)
        patientDao.insertPatient(patientWithTimestamp.toEntity())

        // Try to sync to Firestore (don't throw if it fails - data is already in Room)
        val firestoreResult = firestorePatientDataSource.savePatient(patientWithTimestamp)
        firestoreResult.onFailure { e ->
            // Log error but don't throw - local save succeeded
            println("Failed to sync patient profile to Firestore: ${e.message}")
        }
    }

    /**
     * Sync patient profile from Firestore to Room
     */
    override suspend fun syncPatientProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        try {
            // Fetch from Firestore
            val firestorePatient = firestorePatientDataSource.getPatient(userId)

            if (firestorePatient != null) {
                // Save to Room
                patientDao.insertPatient(firestorePatient.toEntity())
            }
        } catch (e: Exception) {
            // Log error but don't throw - allow app to continue with local data
            println("Failed to sync patient profile: ${e.message}")
        }
    }
}

