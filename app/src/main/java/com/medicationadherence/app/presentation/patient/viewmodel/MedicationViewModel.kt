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

    private val _adherenceHistory = MutableStateFlow<List<AdherenceRecord>>(emptyList())
    val adherenceHistory: StateFlow<List<AdherenceRecord>> = _adherenceHistory.asStateFlow()

    init {
        loadTodayMedications()
    }

    /**
     * Load today's medications
     */
    fun loadTodayMedications() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                medicationRepository.getTodayMedications().collect { medications ->
                    _todayMedications.value = medications
                    _isLoading.value = false
                }
            } catch (exception: Exception) {
                _errorMessage.value = exception.message
                _isLoading.value = false
            }
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
                android.util.Log.d("MedicationViewModel", "Adding medication: name=$name, dosage=$dosage, frequency=$frequency")
                
                val medication = Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    instructions = instructions
                )
                
                val medicationId = medicationRepository.insertMedication(medication)
                android.util.Log.d("MedicationViewModel", "Medication added successfully with ID: $medicationId")
                
                _medicationAdded.value = true
                _isLoading.value = false
                
                // Reload medications to show the new one
                loadTodayMedications()
            } catch (e: Exception) {
                android.util.Log.e("MedicationViewModel", "Error adding medication", e)
                _errorMessage.value = e.message ?: "Failed to add medication"
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
                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                val javaWeekAgo = javaToday.minusDays(7)
                
                val today = kotlinx.datetime.LocalDate(javaToday.year, javaToday.monthValue, javaToday.dayOfMonth)
                val weekAgo = kotlinx.datetime.LocalDate(javaWeekAgo.year, javaWeekAgo.monthValue, javaWeekAgo.dayOfMonth)
                
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

    /**
     * Load adherence history for a medication
     * @param medicationId The medication ID
     * @param days Number of days to look back (default 7)
     */
    fun loadAdherenceHistory(medicationId: String, days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = java.time.LocalDateTime.now()
                val javaEndDate = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                val javaStartDate = javaEndDate.minusDays(days.toLong())
                
                val endDate = kotlinx.datetime.LocalDate(javaEndDate.year, javaEndDate.monthValue, javaEndDate.dayOfMonth)
                val startDate = kotlinx.datetime.LocalDate(javaStartDate.year, javaStartDate.monthValue, javaStartDate.dayOfMonth)
                
                medicationRepository.getAdherenceHistory(medicationId, startDate, endDate)
                    .onEach { history ->
                        _adherenceHistory.value = history.sortedByDescending { it.date }
                        _isLoading.value = false
                    }
                    .catch { exception ->
                        _errorMessage.value = exception.message
                        _isLoading.value = false
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear adherence history
     */
    fun clearAdherenceHistory() {
        _adherenceHistory.value = emptyList()
    }
}
