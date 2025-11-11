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

    private val _adherenceStats = MutableStateFlow<AdherenceStats?>(null)
    val adherenceStats: StateFlow<AdherenceStats?> = _adherenceStats.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _weeklyAdherence = MutableStateFlow<List<DailyAdherence>>(emptyList())
    val weeklyAdherence: StateFlow<List<DailyAdherence>> = _weeklyAdherence.asStateFlow()

    private val _monthlyAdherence = MutableStateFlow<List<DailyAdherence>>(emptyList())
    val monthlyAdherence: StateFlow<List<DailyAdherence>> = _monthlyAdherence.asStateFlow()

    init {
        loadTodayMedications()
        loadAdherenceStats()
        loadCurrentStreak()
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

    /**
     * Load adherence statistics (30-day rate, doses taken, etc.)
     */
    fun loadAdherenceStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = java.time.LocalDateTime.now()
                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                val java30DaysAgo = javaToday.minusDays(30)
                
                val today = kotlinx.datetime.LocalDate(javaToday.year, javaToday.monthValue, javaToday.dayOfMonth)
                val thirtyDaysAgo = kotlinx.datetime.LocalDate(java30DaysAgo.year, java30DaysAgo.monthValue, java30DaysAgo.dayOfMonth)
                
                // Get all medications
                val medications = medicationRepository.getAllMedications().first()
                
                // Calculate total expected doses and taken doses
                var totalExpectedDoses = 0
                var totalTakenDoses = 0
                
                medications.forEach { medication ->
                    val history = medicationRepository.getAdherenceHistory(medication.id, thirtyDaysAgo, today).first()
                    totalExpectedDoses += (medication.frequency.size * 30)
                    totalTakenDoses += history.count { it.status == AdherenceStatus.TAKEN }
                }
                
                val adherencePercentage = if (totalExpectedDoses > 0) {
                    ((totalTakenDoses.toFloat() / totalExpectedDoses.toFloat()) * 100).toInt()
                } else 0
                
                _adherenceStats.value = AdherenceStats(
                    adherencePercentage = adherencePercentage,
                    totalDoses = totalExpectedDoses,
                    takenDoses = totalTakenDoses
                )
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load current streak (consecutive days with all medications taken)
     */
    fun loadCurrentStreak() {
        viewModelScope.launch {
            try {
                val now = java.time.LocalDateTime.now()
                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                
                // Get all medications
                val medications = medicationRepository.getAllMedications().first()
                if (medications.isEmpty()) {
                    _currentStreak.value = 0
                    return@launch
                }
                
                var streak = 0
                var currentDay = javaToday
                
                // Count backwards from today until we find a day with missed doses
                while (true) {
                    val kotlinDate = kotlinx.datetime.LocalDate(currentDay.year, currentDay.monthValue, currentDay.dayOfMonth)
                    
                    var allTaken = true
                    for (medication in medications) {
                        val history = medicationRepository.getAdherenceHistory(medication.id, kotlinDate, kotlinDate).first()
                        val expectedDoses = medication.frequency.size
                        val takenDoses = history.count { it.status == AdherenceStatus.TAKEN }
                        
                        if (takenDoses < expectedDoses) {
                            allTaken = false
                            break
                        }
                    }
                    
                    if (!allTaken) break
                    
                    streak++
                    currentDay = currentDay.minusDays(1)
                    
                    // Limit to 365 days to prevent infinite loop
                    if (streak >= 365) break
                }
                
                _currentStreak.value = streak
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _currentStreak.value = 0
            }
        }
    }

    /**
     * Load weekly adherence data
     */
    fun loadWeeklyAdherence() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = java.time.LocalDateTime.now()
                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                val java7DaysAgo = javaToday.minusDays(6)
                
                val medications = medicationRepository.getAllMedications().first()
                val weeklyData = mutableListOf<DailyAdherence>()
                
                for (i in 0..6) {
                    val javaDate = java7DaysAgo.plusDays(i.toLong())
                    val kotlinDate = kotlinx.datetime.LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
                    
                    var totalExpected = 0
                    var totalTaken = 0
                    
                    medications.forEach { medication ->
                        val history = medicationRepository.getAdherenceHistory(medication.id, kotlinDate, kotlinDate).first()
                        totalExpected += medication.frequency.size
                        totalTaken += history.count { it.status == AdherenceStatus.TAKEN }
                    }
                    
                    val percentage = if (totalExpected > 0) {
                        ((totalTaken.toFloat() / totalExpected.toFloat()) * 100).toInt()
                    } else 0
                    
                    weeklyData.add(DailyAdherence(kotlinDate, percentage))
                }
                
                _weeklyAdherence.value = weeklyData
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load monthly adherence data (by weeks)
     */
    fun loadMonthlyAdherence() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = java.time.LocalDateTime.now()
                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                val java30DaysAgo = javaToday.minusDays(29)
                
                val medications = medicationRepository.getAllMedications().first()
                val monthlyData = mutableListOf<DailyAdherence>()
                
                // Group by weeks (4 weeks)
                for (week in 0..3) {
                    val weekStart = java30DaysAgo.plusDays((week * 7).toLong())
                    val weekEnd = weekStart.plusDays(6)
                    
                    var totalExpected = 0
                    var totalTaken = 0
                    
                    medications.forEach { medication ->
                        val kotlinStart = kotlinx.datetime.LocalDate(weekStart.year, weekStart.monthValue, weekStart.dayOfMonth)
                        val kotlinEnd = kotlinx.datetime.LocalDate(weekEnd.year, weekEnd.monthValue, weekEnd.dayOfMonth)
                        
                        val history = medicationRepository.getAdherenceHistory(medication.id, kotlinStart, kotlinEnd).first()
                        totalExpected += (medication.frequency.size * 7)
                        totalTaken += history.count { it.status == AdherenceStatus.TAKEN }
                    }
                    
                    val percentage = if (totalExpected > 0) {
                        ((totalTaken.toFloat() / totalExpected.toFloat()) * 100).toInt()
                    } else 0
                    
                    val kotlinWeekStart = kotlinx.datetime.LocalDate(weekStart.year, weekStart.monthValue, weekStart.dayOfMonth)
                    monthlyData.add(DailyAdherence(kotlinWeekStart, percentage))
                }
                
                _monthlyAdherence.value = monthlyData
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Get adherence for a specific date (for calendar view)
     */
    suspend fun getAdherenceForDate(date: kotlinx.datetime.LocalDate): Boolean {
        return try {
            val medications = medicationRepository.getAllMedications().first()
            if (medications.isEmpty()) return false
            
            var allTaken = true
            for (medication in medications) {
                val history = medicationRepository.getAdherenceHistory(medication.id, date, date).first()
                val expectedDoses = medication.frequency.size
                val takenDoses = history.count { it.status == AdherenceStatus.TAKEN }
                
                if (takenDoses < expectedDoses) {
                    allTaken = false
                    break
                }
            }
            allTaken
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class for adherence statistics
 */
data class AdherenceStats(
    val adherencePercentage: Int,
    val totalDoses: Int,
    val takenDoses: Int
)

/**
 * Data class for daily adherence
 */
data class DailyAdherence(
    val date: kotlinx.datetime.LocalDate,
    val percentage: Int
)
