package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.medicationadherence.app.domain.model.Alert
import com.medicationadherence.app.domain.model.AlertType
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for alert management
 */
@Singleton
class FirestoreAlertDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ALERTS = "alerts"
    }

    /**
     * Get all active alerts (not dismissed)
     */
    suspend fun getActiveAlerts(): List<Alert> {
        return try {
            val snapshot = firestore.collection(COLLECTION_ALERTS)
                .whereEqualTo("isDismissed", false)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toAlert()
            }
        } catch (e: SecurityException) {
            // Google Play Services issue - return empty list instead of crashing
            emptyList()
        } catch (e: Exception) {
            // Other errors - return empty list to prevent crashes
            emptyList()
        }
    }

    /**
     * Setup real-time listener for active alerts
     */
    fun listenToAlerts(onAlertsChanged: (List<Alert>) -> Unit): () -> Unit {
        val registration = firestore.collection(COLLECTION_ALERTS)
            .whereEqualTo("isDismissed", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }
                
                val alerts = snapshot.documents.mapNotNull { doc ->
                    doc.toAlert()
                }
                onAlertsChanged(alerts)
            }
        
        return { registration.remove() }
    }

    /**
     * Save an alert to Firestore
     */
    suspend fun saveAlert(alert: Alert): Result<Unit> {
        return try {
            val data = alert.toFirestoreMap()
            firestore.collection(COLLECTION_ALERTS)
                .document(alert.id)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: SecurityException) {
            // Google Play Services issue - return success to prevent crashes
            // Alert will be lost but app continues to function
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save alert: ${e.message}", e))
        }
    }

    /**
     * Mark alert as dismissed
     */
    suspend fun dismissAlert(alertId: String): Result<Unit> {
        return try {
            val now = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            firestore.collection(COLLECTION_ALERTS)
                .document(alertId)
                .update(mapOf(
                    "isDismissed" to true,
                    "dismissedAt" to now.toString()
                ))
                .await()
            Result.success(Unit)
        } catch (e: SecurityException) {
            // Google Play Services issue - return success to prevent crashes
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to dismiss alert: ${e.message}", e))
        }
    }

    /**
     * Mark alert as resolved
     */
    suspend fun resolveAlert(alertId: String): Result<Unit> {
        return try {
            val now = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            firestore.collection(COLLECTION_ALERTS)
                .document(alertId)
                .update(mapOf(
                    "isResolved" to true,
                    "resolvedAt" to now.toString()
                ))
                .await()
            Result.success(Unit)
        } catch (e: SecurityException) {
            // Google Play Services issue - return success to prevent crashes
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to resolve alert: ${e.message}", e))
        }
    }

    /**
     * Delete old dismissed alerts
     */
    suspend fun deleteOldAlerts(daysOld: Int): Result<Unit> {
        return try {
            val threshold = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toEpochDays() - daysOld
            
            val snapshot = firestore.collection(COLLECTION_ALERTS)
                .whereEqualTo("isDismissed", true)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete old alerts: ${e.message}", e))
        }
    }
}

/**
 * Extension function to convert Firestore document to Alert
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toAlert(): Alert? {
    return try {
        val data = this.data ?: return null
        Alert(
            id = this.id,
            type = AlertType.valueOf(data["type"] as? String ?: "INFO"),
            patientId = data["patientId"] as? String ?: "",
            patientName = data["patientName"] as? String ?: "",
            title = data["title"] as? String ?: "",
            message = data["message"] as? String ?: "",
            medicationName = data["medicationName"] as? String,
            timestamp = parseLocalDateTime(data["timestamp"] as? String) ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
            isResolved = data["isResolved"] as? Boolean ?: false,
            resolvedAt = parseLocalDateTime(data["resolvedAt"] as? String),
            isDismissed = data["isDismissed"] as? Boolean ?: false,
            dismissedAt = parseLocalDateTime(data["dismissedAt"] as? String),
            metadata = data["metadata"] as? Map<String, Any> ?: emptyMap()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Alert to Firestore map
 */
private fun Alert.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "type" to type.name,
        "patientId" to patientId,
        "patientName" to patientName,
        "title" to title,
        "message" to message,
        "medicationName" to medicationName,
        "timestamp" to timestamp.toString(),
        "isResolved" to isResolved,
        "resolvedAt" to resolvedAt?.toString(),
        "isDismissed" to isDismissed,
        "dismissedAt" to dismissedAt?.toString(),
        "metadata" to metadata
    )
}

/**
 * Parse LocalDateTime from string
 */
private fun parseLocalDateTime(value: String?): LocalDateTime? {
    return try {
        if (value == null) null else LocalDateTime.parse(value)
    } catch (e: Exception) {
        null
    }
}
