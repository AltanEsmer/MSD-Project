package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for FCM token management
 */
@Singleton
class FirestoreTokenDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_PATIENTS = "patients"
        private const val FIELD_FCM_TOKEN = "fcmToken"
        private const val FIELD_FCM_TOKEN_UPDATED_AT = "fcmTokenUpdatedAt"
    }

    /**
     * Save or update FCM token for a user
     */
    suspend fun saveFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            val updates = mapOf(
                FIELD_FCM_TOKEN to token,
                FIELD_FCM_TOKEN_UPDATED_AT to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // If document doesn't exist, create it
            try {
                val data = mapOf(
                    FIELD_FCM_TOKEN to token,
                    FIELD_FCM_TOKEN_UPDATED_AT to com.google.firebase.Timestamp.now()
                )
                
                firestore.collection(COLLECTION_PATIENTS)
                    .document(userId)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(Exception("Failed to save FCM token: ${e2.message}", e2))
            }
        }
    }

    /**
     * Get FCM token for a user
     */
    suspend fun getFcmToken(userId: String): String? {
        return try {
            val document = firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .get()
                .await()
            
            document.getString(FIELD_FCM_TOKEN)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete FCM token for a user
     */
    suspend fun deleteFcmToken(userId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                FIELD_FCM_TOKEN to com.google.firebase.firestore.FieldValue.delete(),
                FIELD_FCM_TOKEN_UPDATED_AT to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection(COLLECTION_PATIENTS)
                .document(userId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete FCM token: ${e.message}", e))
        }
    }
}

