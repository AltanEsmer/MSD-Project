package com.medicationadherence.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.firestore.PatientAdherenceStats
import com.medicationadherence.app.domain.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Family Dashboard
 */
@HiltViewModel
class FamilyDashboardViewModel @Inject constructor(
    private val firestorePatientDataSource: FirestorePatientDataSource
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPatients()
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
}

