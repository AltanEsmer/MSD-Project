package com.medicationadherence.app.presentation.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.firestore.FirestoreReportsDataSource
import com.medicationadherence.app.domain.model.AdherenceReport
import com.medicationadherence.app.domain.repository.MedicationRepository
import com.medicationadherence.app.presentation.family.ReportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * ViewModel for Patient Reports Screen
 * Shows adherence reports for the current authenticated user
 */
@HiltViewModel
class PatientReportsViewModel @Inject constructor(
    private val reportsDataSource: FirestoreReportsDataSource,
    private val patientDataSource: FirestorePatientDataSource,
    private val medicationRepository: MedicationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEK)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _report = MutableStateFlow<AdherenceReport?>(null)
    val report: StateFlow<AdherenceReport?> = _report.asStateFlow()

    private val _patientName = MutableStateFlow<String?>(null)
    val patientName: StateFlow<String?> = _patientName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPatientName()
        loadReport()
    }

    /**
     * Load current patient name
     */
    private fun loadPatientName() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId != null) {
                    val patient = patientDataSource.getPatient(userId)
                    _patientName.value = patient?.name
                }
            } catch (e: SecurityException) {
                // Google Play Services issue - silently fail, name is optional
            } catch (e: Exception) {
                // Silently fail - name is optional
            }
        }
    }

    /**
     * Change the report period and reload
     */
    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        loadReport()
    }

    /**
     * Load report for current user and selected period
     */
    fun loadReport() {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            _errorMessage.value = "User not authenticated"
            return
        }
        val period = _selectedPeriod.value
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Sync local data to Firestore first to ensure reports have data
                try {
                    medicationRepository.syncMedications()
                    medicationRepository.syncAdherenceRecords()
                } catch (e: Exception) {
                    // Silently fail sync - reports will use whatever data is in Firestore
                    android.util.Log.d("PatientReportsViewModel", "Sync failed: ${e.message}")
                }
                
                val (startDate, endDate) = getDateRangeForPeriod(period)
                val reportData = reportsDataSource.getAdherenceReport(
                    patientId = userId,
                    startDate = startDate,
                    endDate = endDate
                )
                
                // If report shows no data, check if user has medications locally
                if (reportData.totalDoses == 0) {
                    val localMedications = medicationRepository.getAllMedications().first()
                    if (localMedications.isEmpty()) {
                        _errorMessage.value = "No medications found. Add medications to see your adherence report."
                    } else {
                        _errorMessage.value = "No adherence data found for the selected period. Start taking medications to see your report."
                    }
                }
                
                _report.value = reportData
                _isLoading.value = false
            } catch (e: SecurityException) {
                // Google Play Services issue - show empty report instead of error
                // The report will show "Unable to generate report" message from FirestoreReportsDataSource
                _report.value = null
                _errorMessage.value = null // Don't show error for GPS issues
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load report: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh report data
     */
    fun refresh() {
        loadPatientName()
        loadReport()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get date range based on selected period
     */
    private fun getDateRangeForPeriod(period: ReportPeriod): Pair<LocalDate, LocalDate> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val endDate = now.date
        
        val startDate = when (period) {
            ReportPeriod.WEEK -> endDate.minus(7, DateTimeUnit.DAY)
            ReportPeriod.MONTH -> endDate.minus(30, DateTimeUnit.DAY)
            ReportPeriod.THREE_MONTHS -> endDate.minus(90, DateTimeUnit.DAY)
        }
        
        return Pair(startDate, endDate)
    }

    /**
     * Export report as text (for sharing via email/messaging)
     */
    fun exportReportAsText(): String {
        val report = _report.value ?: return "No report available"
        
        return buildString {
            appendLine("=== MEDICATION ADHERENCE REPORT ===")
            appendLine()
            appendLine("Patient: ${report.patientName}")
            appendLine("Period: ${report.startDate} to ${report.endDate}")
            appendLine()
            appendLine("--- SUMMARY ---")
            appendLine("Overall Adherence: ${report.overallAdherenceRate.toInt()}%")
            appendLine("Total Doses: ${report.totalDoses}")
            appendLine("Taken: ${report.takenDoses}")
            appendLine("Missed: ${report.missedDoses}")
            appendLine("Current Streak: ${report.currentStreak} days")
            appendLine("Best Streak: ${report.bestStreak} days")
            appendLine()
            appendLine("--- MEDICATION BREAKDOWN ---")
            report.medicationBreakdown.forEach { med ->
                appendLine("${med.medicationName}: ${med.adherenceRate.toInt()}% (${med.takenDoses}/${med.totalDoses})")
            }
            appendLine()
            appendLine("--- INSIGHTS ---")
            report.insights.forEach { insight ->
                appendLine("• $insight")
            }
            appendLine()
            appendLine("Generated by Medication Adherence App")
        }
    }
}

