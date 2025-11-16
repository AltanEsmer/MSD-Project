package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.medicationadherence.app.data.firestore.mapper.toAdherenceRecord
import com.medicationadherence.app.data.firestore.mapper.toFirestoreMap
import com.medicationadherence.app.domain.model.AdherenceRecord
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for adherence record management
 */
@Singleton
class FirestoreAdherenceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ADHERENCE = "adherence_records"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_MEDICATION_ID = "medicationId"
        private const val FIELD_DATE = "date"
    }

    /**
     * Get adherence records for a medication within a date range
     */
    suspend fun getAdherenceRecords(
        userId: String,
        medicationId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AdherenceRecord> {
        return try {
            val snapshot = firestore.collection(COLLECTION_ADHERENCE)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_MEDICATION_ID, medicationId)
                .whereGreaterThanOrEqualTo(FIELD_DATE, startDate.toString())
                .whereLessThanOrEqualTo(FIELD_DATE, endDate.toString())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toAdherenceRecord()
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch adherence records: ${e.message}", e)
        }
    }

    /**
     * Save adherence record
     */
    suspend fun saveAdherenceRecord(userId: String, record: AdherenceRecord): Result<Unit> {
        return try {
            val data = record.toFirestoreMap().toMutableMap()
            data[FIELD_USER_ID] = userId

            firestore.collection(COLLECTION_ADHERENCE)
                .document(record.id)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save adherence record: ${e.message}", e))
        }
    }

    /**
     * Batch save multiple adherence records
     */
    suspend fun saveAdherenceRecords(userId: String, records: List<AdherenceRecord>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            records.forEach { record ->
                val data = record.toFirestoreMap().toMutableMap()
                data[FIELD_USER_ID] = userId
                val ref = firestore.collection(COLLECTION_ADHERENCE).document(record.id)
                batch.set(ref, data)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save adherence records: ${e.message}", e))
        }
    }

    /**
     * Delete adherence record
     */
    suspend fun deleteAdherenceRecord(userId: String, recordId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_ADHERENCE)
                .document(recordId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete adherence record: ${e.message}", e))
        }
    }

    /**
     * Get recent adherence records for all patients (for activity timeline)
     */
    suspend fun getRecentAdherenceRecordsForAllPatients(days: Int = 7): List<AdherenceRecordWithPatient> {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            // Use java.time for date arithmetic (same pattern as rest of codebase)
            val javaDate = java.time.LocalDate.of(now.date.year, now.date.monthNumber, now.date.dayOfMonth)
                .minusDays(days.toLong())
            val startDate = LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth).toString()

            val snapshot = firestore.collection(COLLECTION_ADHERENCE)
                .whereGreaterThanOrEqualTo(FIELD_DATE, startDate)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val userId = data[FIELD_USER_ID] as? String ?: return@mapNotNull null
                    val record = data.toAdherenceRecord() ?: return@mapNotNull null
                    AdherenceRecordWithPatient(
                        userId = userId,
                        record = record,
                        medicationName = data["medicationName"] as? String
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch recent adherence records: ${e.message}", e)
        }
    }

    /**
     * Get all adherence records for a specific user
     */
    suspend fun getAllAdherenceRecordsForUser(userId: String): List<AdherenceRecord> {
        return try {
            val snapshot = firestore.collection(COLLECTION_ADHERENCE)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toAdherenceRecord()
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch adherence records for user: ${e.message}", e)
        }
    }
}

/**
 * Data class for adherence record with patient information
 */
data class AdherenceRecordWithPatient(
    val userId: String,
    val record: AdherenceRecord,
    val medicationName: String?
)

