package com.medicationadherence.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.data.firestore.AdherenceRecordWithPatient
import com.medicationadherence.app.data.firestore.FirestoreAdherenceDataSource
import com.medicationadherence.app.data.firestore.FirestoreAlertDataSource
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.Alert
import com.medicationadherence.app.domain.model.AlertType
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

/**
 * ViewModel for Family Alerts Screen
 */
@HiltViewModel
class FamilyAlertsViewModel @Inject constructor(
    private val alertDataSource: FirestoreAlertDataSource,
    private val adherenceDataSource: FirestoreAdherenceDataSource,
    private val patientDataSource: FirestorePatientDataSource
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _patientContacts = MutableStateFlow<Map<String, String>>(emptyMap())

    private var alertsListener: (() -> Unit)? = null

    init {
        // Load data asynchronously to avoid blocking initialization
        viewModelScope.launch {
            try {
                loadAlerts()
                setupRealtimeListener()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize alerts: ${e.message}"
            }
        }
    }

    /**
     * Setup real-time listener for alerts
     */
    private fun setupRealtimeListener() {
        viewModelScope.launch {
            try {
                alertsListener = alertDataSource.listenToAlerts { alerts ->
                    viewModelScope.launch {
                        _alerts.value = alerts
                            .distinctBy { "${it.patientId}-${it.type}-${it.medicationName}" }
                            .sortedWith(compareByDescending<Alert> { it.type.ordinal }.thenByDescending { it.timestamp })
                        
                        // Load contact info for all patients
                        loadPatientContacts(alerts.map { it.patientId }.distinct())
                    }
                }
            } catch (e: Exception) {
                // Real-time updates not available, continue with manual refresh
            }
        }
    }

    /**
     * Load patient contact information
     */
    private suspend fun loadPatientContacts(patientIds: List<String>) {
        val contacts = mutableMapOf<String, String>()
        patientIds.forEach { patientId ->
            try {
                val patient = patientDataSource.getPatient(patientId)
                patient?.emergencyContact?.let { contact ->
                    contacts[patientId] = contact
                }
            } catch (e: Exception) {
                // Failed to get patient contact
            }
        }
        _patientContacts.value = contacts
    }

    override fun onCleared() {
        super.onCleared()
        alertsListener?.invoke()
    }

    /**
     * Load alerts from Firestore and generate new ones from adherence data
     */
    fun loadAlerts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Load existing alerts (may return empty list if Firestore unavailable)
                val existingAlerts = try {
                    alertDataSource.getActiveAlerts()
                } catch (e: Exception) {
                    emptyList()
                }

                // Generate new alerts from adherence data (may fail silently)
                val generatedAlerts = try {
                    generateAlertsFromAdherenceData()
                } catch (e: Exception) {
                    emptyList()
                }

                // Combine and deduplicate
                val allAlerts = (existingAlerts + generatedAlerts)
                    .distinctBy { "${it.patientId}-${it.type}-${it.medicationName}" }
                    .sortedWith(compareByDescending<Alert> { it.type.ordinal }.thenByDescending { it.timestamp })

                _alerts.value = allAlerts

                // Save newly generated alerts to Firestore (fail silently if unavailable)
                generatedAlerts.forEach { alert ->
                    try {
                        alertDataSource.saveAlert(alert)
                    } catch (e: Exception) {
                        // Silently fail - alert won't be persisted but app continues
                    }
                }

                // Load patient contacts
                loadPatientContacts(allAlerts.map { it.patientId }.distinct())
            } catch (e: SecurityException) {
                // Google Play Services issue - show empty alerts instead of error
                _alerts.value = emptyList()
                _errorMessage.value = null // Don't show error for GPS issues
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load alerts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate alerts from adherence records
     */
    private suspend fun generateAlertsFromAdherenceData(): List<Alert> {
        val alerts = mutableListOf<Alert>()
        
        try {
            // Get all patients with sharing enabled (may fail if Firestore unavailable)
            val patients = try {
                patientDataSource.getAllPatientsWithSharingEnabled()
            } catch (e: Exception) {
                return emptyList() // Return empty if can't get patients
            }
            
            patients.forEach { patient ->
                // Get adherence records for this patient (may fail silently)
                val adherenceRecords = try {
                    adherenceDataSource.getAllAdherenceRecordsForUser(patient.id)
                } catch (e: Exception) {
                    emptyList() // Continue with empty records
                }
                
                // Check for missed doses today
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                val todayRecords = adherenceRecords.filter { it.date == today }
                val missedToday = todayRecords.filter { it.status == AdherenceStatus.MISSED }
                
                if (missedToday.isNotEmpty()) {
                    missedToday.forEach { record ->
                        alerts.add(
                            Alert(
                                id = UUID.randomUUID().toString(),
                                type = AlertType.CRITICAL,
                                patientId = patient.id,
                                patientName = patient.name,
                                title = "Missed Medication",
                                message = "Missed dose detected today",
                                medicationName = record.id,
                                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                                metadata = mapOf("date" to today.toString())
                            )
                        )
                    }
                }
                
                // Check for consecutive missed doses
                val recentRecords = adherenceRecords
                    .sortedByDescending { it.date }
                    .take(10)
                var consecutiveMissed = 0
                for (record in recentRecords) {
                    if (record.status == AdherenceStatus.MISSED) {
                        consecutiveMissed++
                    } else {
                        break
                    }
                }
                
                if (consecutiveMissed >= 2) {
                    alerts.add(
                        Alert(
                            id = UUID.randomUUID().toString(),
                            type = AlertType.CRITICAL,
                            patientId = patient.id,
                            patientName = patient.name,
                            title = "Multiple Missed Doses",
                            message = "$consecutiveMissed consecutive doses missed",
                            timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            metadata = mapOf("consecutiveMissed" to consecutiveMissed)
                        )
                    )
                }
                
                // Check adherence rate (may fail if Firestore unavailable)
                val stats = try {
                    patientDataSource.getPatientAdherenceStats(patient.id)
                } catch (e: Exception) {
                    return@forEach // Skip this patient if stats can't be loaded
                }
                if (stats.adherenceRate < 80 && adherenceRecords.size > 5) {
                    alerts.add(
                        Alert(
                            id = UUID.randomUUID().toString(),
                            type = AlertType.WARNING,
                            patientId = patient.id,
                            patientName = patient.name,
                            title = "Low Adherence Rate",
                            message = "Adherence rate is ${stats.adherenceRate}%",
                            timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            metadata = mapOf("adherenceRate" to stats.adherenceRate)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the whole operation
        }
        
        return alerts
    }

    /**
     * Dismiss an alert
     */
    fun dismissAlert(alertId: String) {
        viewModelScope.launch {
            try {
                alertDataSource.dismissAlert(alertId)
                _alerts.value = _alerts.value.filter { it.id != alertId }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to dismiss alert: ${e.message}"
            }
        }
    }

    /**
     * Resolve an alert
     */
    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            try {
                alertDataSource.resolveAlert(alertId)
                _alerts.value = _alerts.value.map { alert ->
                    if (alert.id == alertId) {
                        alert.copy(
                            isResolved = true,
                            resolvedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        )
                    } else {
                        alert
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to resolve alert: ${e.message}"
            }
        }
    }

    /**
     * Get filtered alerts by type
     */
    fun getFilteredAlerts(filter: AlertFilter): List<Alert> {
        return when (filter) {
            AlertFilter.ALL -> _alerts.value
            AlertFilter.CRITICAL -> _alerts.value.filter { it.type == AlertType.CRITICAL }
            AlertFilter.MISSED_DOSES -> _alerts.value.filter { 
                it.title.contains("Missed", ignoreCase = true) 
            }
            AlertFilter.LOW_ADHERENCE -> _alerts.value.filter { 
                it.title.contains("Adherence", ignoreCase = true) 
            }
        }
    }

    /**
     * Get active alerts count
     */
    fun getActiveAlertsCount(): Int {
        return _alerts.value.count { !it.isResolved && !it.isDismissed }
    }

    /**
     * Get patient phone number for contact
     */
    fun getPatientPhoneNumber(patientId: String): String? {
        return _patientContacts.value[patientId]
    }
}

/**
 * Alert filter types
 */
enum class AlertFilter {
    ALL,
    CRITICAL,
    MISSED_DOSES,
    LOW_ADHERENCE
}
