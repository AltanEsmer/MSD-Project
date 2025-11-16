package com.medicationadherence.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.data.firestore.FirestoreAdherenceDataSource
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.firestore.PatientAdherenceStats
import com.medicationadherence.app.domain.model.ActivityItem
import com.medicationadherence.app.domain.model.ActivityType
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * ViewModel for Family Dashboard
 */
@HiltViewModel
class FamilyDashboardViewModel @Inject constructor(
    private val firestorePatientDataSource: FirestorePatientDataSource,
    private val firestoreAdherenceDataSource: FirestoreAdherenceDataSource
) : ViewModel() {

    data class PatientWithStats(
        val patient: Patient,
        val adherenceRate: Int,
        val todayTaken: Int,
        val todayTotal: Int,
        val missedDoses: Int,
        val lastUpdate: String,
        val status: String // "good" or "attention"
    )

    private val _patients = MutableStateFlow<List<PatientWithStats>>(emptyList())
    val patients: StateFlow<List<PatientWithStats>> = _patients.asStateFlow()

    private val _recentActivity = MutableStateFlow<List<ActivityItem>>(emptyList())
    val recentActivity: StateFlow<List<ActivityItem>> = _recentActivity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load data asynchronously to avoid blocking initialization
        viewModelScope.launch {
            try {
                loadPatients()
                loadRecentActivity()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize: ${e.message}"
            }
        }
    }

    fun loadPatients() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Fetch all patients with sharing enabled
                val allPatients = firestorePatientDataSource.getAllPatientsWithSharingEnabled()

                // Fetch adherence stats for each patient
                val patientsWithStats = allPatients.map { patient ->
                    val stats = firestorePatientDataSource.getPatientAdherenceStats(patient.id)
                    PatientWithStats(
                        patient = patient,
                        adherenceRate = stats.adherenceRate,
                        todayTaken = stats.todayTaken,
                        todayTotal = stats.todayTotal.coerceAtLeast(1), // Avoid division by zero
                        missedDoses = stats.missedDoses,
                        lastUpdate = stats.lastUpdate,
                        status = if (stats.adherenceRate >= 80 && stats.missedDoses == 0) "good" else "attention"
                    )
                }

                _patients.value = patientsWithStats
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load patients: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load recent activity from adherence records
     */
    fun loadRecentActivity() {
        viewModelScope.launch {
            try {
                // Get recent adherence records
                val recentRecords = firestoreAdherenceDataSource.getRecentAdherenceRecordsForAllPatients(7)
                
                // Get patient information
                val patients = firestorePatientDataSource.getAllPatientsWithSharingEnabled()
                val patientMap = patients.associateBy { it.id }
                
                // Transform to activity items
                val activities = recentRecords.mapNotNull { recordWithPatient ->
                    val patient = patientMap[recordWithPatient.userId] ?: return@mapNotNull null
                    val record = recordWithPatient.record
                    
                    val activityType = when (record.status) {
                        AdherenceStatus.TAKEN -> ActivityType.TOOK_MEDICATION
                        AdherenceStatus.MISSED -> ActivityType.MISSED_DOSE
                        AdherenceStatus.SKIPPED -> ActivityType.SKIPPED_MEDICATION
                        else -> null
                    } ?: return@mapNotNull null
                    
                    val action = when (activityType) {
                        ActivityType.TOOK_MEDICATION -> "Took medication"
                        ActivityType.MISSED_DOSE -> "Missed medication dose"
                        ActivityType.SKIPPED_MEDICATION -> "Skipped medication"
                    }
                    
                    ActivityItem(
                        id = UUID.randomUUID().toString(),
                        patientId = patient.id,
                        patientName = patient.name,
                        action = action,
                        actionType = activityType,
                        medicationName = recordWithPatient.medicationName,
                        timestamp = record.timestamp ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                
                // Sort by timestamp descending and take last 15
                _recentActivity.value = activities
                    .sortedByDescending { it.timestamp }
                    .take(15)
            } catch (e: Exception) {
                // Don't fail if activity loading fails
            }
        }
    }

    fun getAverageAdherence(): Int {
        val patientsList = _patients.value
        return if (patientsList.isEmpty()) {
            0
        } else {
            patientsList.map { it.adherenceRate }.average().toInt()
        }
    }

    fun getActiveAlertsCount(): Int {
        return _patients.value.count { it.status == "attention" }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadPatients()
        loadRecentActivity()
    }

    /**
     * Format timestamp to relative time
     */
    fun formatRelativeTime(timestamp: kotlinx.datetime.LocalDateTime): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Calculate difference (simplified)
        val daysDiff = now.date.toEpochDays() - timestamp.date.toEpochDays()
        
        return when {
            daysDiff == 0 -> {
                val hoursDiff = now.hour - timestamp.hour
                when {
                    hoursDiff == 0 -> "Just now"
                    hoursDiff == 1 -> "1 hour ago"
                    hoursDiff < 24 -> "$hoursDiff hours ago"
                    else -> "Today"
                }
            }
            daysDiff == 1 -> "Yesterday"
            daysDiff < 7 -> "$daysDiff days ago"
            else -> "${daysDiff / 7} weeks ago"
        }
    }
}

