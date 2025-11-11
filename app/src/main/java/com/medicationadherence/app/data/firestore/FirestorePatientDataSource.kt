package com.medicationadherence.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.medicationadherence.app.data.firestore.mapper.toFirestoreMap
import com.medicationadherence.app.data.firestore.mapper.toPatient
import com.medicationadherence.app.domain.model.Patient
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
     * Get all patients with data sharing enabled (for caregivers)
     */
    suspend fun getAllPatientsWithSharingEnabled(): List<Patient> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PATIENTS)
                .whereEqualTo("shareDataEnabled", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    data.toPatient().copy(id = doc.id)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch patients: ${e.message}", e)
        }
    }

    /**
     * Get adherence statistics for a patient
     */
    suspend fun getPatientAdherenceStats(patientId: String): PatientAdherenceStats {
        return try {
            val adherenceRef = firestore.collection("adherence_records")
            val snapshot = adherenceRef
                .whereEqualTo("userId", patientId)
                .get()
                .await()

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            val todayString = today.toString()

            var todayTaken = 0
            var todayTotal = 0
            var totalTaken = 0
            var totalRecords = 0

            snapshot.documents.forEach { doc ->
                val data = doc.data
                val recordDate = data?.get("date") as? String ?: ""
                val status = data?.get("status") as? String ?: "PENDING"

                totalRecords++
                if (status == "TAKEN") {
                    totalTaken++
                }
                if (recordDate == todayString) {
                    todayTotal++
                    if (status == "TAKEN") {
                        todayTaken++
                    }
                }
            }

            val adherenceRate = if (totalRecords > 0) {
                ((totalTaken.toFloat() / totalRecords) * 100).toInt()
            } else {
                0
            }

            PatientAdherenceStats(
                adherenceRate = adherenceRate,
                todayTaken = todayTaken,
                todayTotal = todayTotal,
                missedDoses = todayTotal - todayTaken,
                lastUpdate = "Recently" // You can enhance this with actual timestamp
            )
        } catch (e: Exception) {
            PatientAdherenceStats(
                adherenceRate = 0,
                todayTaken = 0,
                todayTotal = 0,
                missedDoses = 0,
                lastUpdate = "Unknown"
            )
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

/**
 * Data class for patient adherence statistics
 */
data class PatientAdherenceStats(
    val adherenceRate: Int,
    val todayTaken: Int,
    val todayTotal: Int,
    val missedDoses: Int,
    val lastUpdate: String
)
