package com.medicationadherence.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.firestore.FirestoreReportsDataSource
import com.medicationadherence.app.domain.model.AdherenceReport
import com.medicationadherence.app.domain.model.OverallSummaryStats
import com.medicationadherence.app.domain.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * ViewModel for Family Reports Screen
 */
@HiltViewModel
class FamilyReportsViewModel @Inject constructor(
    private val reportsDataSource: FirestoreReportsDataSource,
    private val patientDataSource: FirestorePatientDataSource
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient: StateFlow<Patient?> = _selectedPatient.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEK)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _report = MutableStateFlow<AdherenceReport?>(null)
    val report: StateFlow<AdherenceReport?> = _report.asStateFlow()

    private val _summaryStats = MutableStateFlow<OverallSummaryStats?>(null)
    val summaryStats: StateFlow<OverallSummaryStats?> = _summaryStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPatients()
        loadSummaryStats()
    }

    /**
     * Load all patients with data sharing enabled
     */
    private fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val patientList = patientDataSource.getAllPatientsWithSharingEnabled()
                _patients.value = patientList
                
                // Auto-select first patient if available
                if (patientList.isNotEmpty() && _selectedPatient.value == null) {
                    selectPatient(patientList.first())
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load patients: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load overall summary statistics
     */
    private fun loadSummaryStats() {
        viewModelScope.launch {
            try {
                val stats = reportsDataSource.getOverallSummaryStats()
                _summaryStats.value = stats
            } catch (e: Exception) {
                // Silently fail - summary stats are optional
            }
        }
    }

    /**
     * Select a patient and load their report
     */
    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
        loadReport()
    }

    /**
     * Change the report period and reload
     */
    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        loadReport()
    }

    /**
     * Load report for selected patient and period
     */
    fun loadReport() {
        val patient = _selectedPatient.value ?: return
        val period = _selectedPeriod.value
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val (startDate, endDate) = getDateRangeForPeriod(period)
                val reportData = reportsDataSource.getAdherenceReport(
                    patientId = patient.id,
                    startDate = startDate,
                    endDate = endDate
                )
                _report.value = reportData
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load report: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadPatients()
        loadSummaryStats()
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

/**
 * Report period options
 */
enum class ReportPeriod {
    WEEK,
    MONTH,
    THREE_MONTHS
}
