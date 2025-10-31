package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.medicationadherence.app.data.firestore.mapper.toFirestoreMap
import com.medicationadherence.app.data.firestore.mapper.toPatient
import com.medicationadherence.app.domain.model.Patient
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for patient profile management
 */
@Singleton
class FirestorePatientDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_PATIENTS = "patients"
    }

    /**
     * Get patient profile by user ID
     */
    suspend fun getPatient(userId: String): Patient? {
        return try {
            val document = firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.data?.toPatient()
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch patient profile: ${e.message}", e)
        }
    }

    /**
     * Save or update patient profile
     */
    suspend fun savePatient(patient: Patient): Result<Unit> {
        return try {
            val data = patient.toFirestoreMap()
            firestore.collection(COLLECTION_PATIENTS)
                .document(patient.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save patient profile: ${e.message}", e))
        }
    }

    /**
     * Update patient profile (merge with existing)
     */
    suspend fun updatePatient(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val mutableUpdates = updates.toMutableMap()
            mutableUpdates["updatedAt"] = com.google.firebase.Timestamp.now()
            
            firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .update(mutableUpdates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update patient profile: ${e.message}", e))
        }
    }

    /**
     * Delete patient profile
     */
    suspend fun deletePatient(userId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete patient profile: ${e.message}", e))
        }
    }
}

