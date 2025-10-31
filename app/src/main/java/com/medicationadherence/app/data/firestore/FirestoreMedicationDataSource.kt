package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.medicationadherence.app.data.firestore.mapper.toFirestoreMap
import com.medicationadherence.app.data.firestore.mapper.toMedication
import com.medicationadherence.app.domain.model.Medication
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for medication management
 */
@Singleton
class FirestoreMedicationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_MEDICATIONS = "medications"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_IS_ACTIVE = "isActive"
    }

    /**
     * Get all medications for a user
     */
    suspend fun getMedications(userId: String): List<Medication> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_ACTIVE, true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toMedication()
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch medications: ${e.message}", e)
        }
    }

    /**
     * Get medications as Flow for real-time updates
     * Note: This can be implemented later if needed using callbackFlow
     */
    // fun getMedicationsFlow(userId: String): Flow<List<Medication>> = callbackFlow { ... }

    /**
     * Get medication by ID
     */
    suspend fun getMedicationById(userId: String, medicationId: String): Medication? {
        return try {
            val document = firestore.collection(COLLECTION_MEDICATIONS)
                .document(medicationId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                // Verify it belongs to the user
                if (data?.get(FIELD_USER_ID) == userId) {
                    data.toMedication()
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch medication: ${e.message}", e)
        }
    }

    /**
     * Save medication (create or update)
     */
    suspend fun saveMedication(userId: String, medication: Medication): Result<String> {
        return try {
            val data = medication.toFirestoreMap().toMutableMap()
            data[FIELD_USER_ID] = userId

            firestore.collection(COLLECTION_MEDICATIONS)
                .document(medication.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(medication.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save medication: ${e.message}", e))
        }
    }

    /**
     * Delete medication (mark as inactive)
     */
    suspend fun deleteMedication(userId: String, medicationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_MEDICATIONS)
                .document(medicationId)
                .update(
                    mapOf(
                        FIELD_IS_ACTIVE to false,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete medication: ${e.message}", e))
        }
    }

    /**
     * Sync medications from Firestore to local (used for initial sync)
     */
    suspend fun syncAllMedications(userId: String): List<Medication> {
        return getMedications(userId)
    }
}

