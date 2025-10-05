package com.medicationadherence.app.presentation.patient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.domain.model.*
import com.medicationadherence.app.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DatePeriod
import javax.inject.Inject

/**
 * ViewModel for medication management in Patient app
 */
@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _todayMedications = MutableStateFlow<List<MedicationWithSchedule>>(emptyList())
    val todayMedications: StateFlow<List<MedicationWithSchedule>> = _todayMedications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _medicationAdded = MutableStateFlow(false)
    val medicationAdded: StateFlow<Boolean> = _medicationAdded.asStateFlow()

    init {
        loadTodayMedications()
    }

    /**
     * Load today's medications
     */
    fun loadTodayMedications() {
        viewModelScope.launch {
            _isLoading.value = true
            medicationRepository.getTodayMedications()
                .onEach { medications ->
                    _todayMedications.value = medications
                    _isLoading.value = false
                }
                .catch { exception ->
                    _errorMessage.value = exception.message
                    _isLoading.value = false
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Add a new medication
     */
    fun addMedication(
        name: String,
        dosage: String,
        frequency: List<String>,
        instructions: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val medication = Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    instructions = instructions
                )
                medicationRepository.insertMedication(medication)
                _medicationAdded.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Update medication
     */
    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                medicationRepository.updateMedication(medication)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete medication
     */
    fun deleteMedication(medicationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                medicationRepository.deleteMedication(medicationId)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Mark medication as taken
     */
    fun takeMedication(scheduleId: String, medicationId: String) {
        viewModelScope.launch {
            try {
                medicationRepository.updateScheduleStatus(scheduleId, AdherenceStatus.TAKEN)
                medicationRepository.logDose(medicationId, AdherenceStatus.TAKEN)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Mark medication as skipped
     */
    fun skipMedication(scheduleId: String, medicationId: String) {
        viewModelScope.launch {
            try {
                medicationRepository.updateScheduleStatus(scheduleId, AdherenceStatus.SKIPPED)
                medicationRepository.logDose(medicationId, AdherenceStatus.SKIPPED)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear medication added flag
     */
    fun clearMedicationAdded() {
        _medicationAdded.value = false
    }

    /**
     * Get adherence rate for a medication
     */
    fun getAdherenceRate(medicationId: String): LiveData<Float> {
        val adherenceRate = MutableLiveData<Float>()
        viewModelScope.launch {
            try {
                val now = java.time.LocalDateTime.now()
                val today = kotlinx.datetime.LocalDate(now.year, now.monthValue, now.dayOfMonth)
                val weekAgo = kotlinx.datetime.LocalDate(
                    today.year, today.month, today.dayOfMonth - 7
                )
                medicationRepository.getAdherenceRate(medicationId, weekAgo, today)
                    .onEach { rate ->
                        adherenceRate.value = rate
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                adherenceRate.value = 0f
            }
        }
        return adherenceRate
    }
}
