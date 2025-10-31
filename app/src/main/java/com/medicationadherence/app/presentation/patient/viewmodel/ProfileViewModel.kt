package com.medicationadherence.app.presentation.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.medicationadherence.app.domain.model.Patient
import com.medicationadherence.app.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for profile management
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _profile = MutableStateFlow<Patient?>(null)
    val profile: StateFlow<Patient?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    /**
     * Load profile from Firestore/Room
     */
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Try to sync from Firestore first
                patientRepository.syncPatientProfile()
                
                // Observe patient data from repository
                patientRepository.getCurrentPatient()
                    .onEach { patient ->
                        _profile.value = patient
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
     * Update profile
     */
    fun updateProfile(
        name: String,
        age: String,
        conditions: List<String>,
        emergencyContact: String,
        bloodType: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentProfile = _profile.value
                val userEmail = firebaseAuth.currentUser?.email ?: currentProfile?.email ?: ""
                val updatedProfile = currentProfile?.copy(
                    name = name,
                    age = age.toIntOrNull() ?: currentProfile.age,
                    conditions = conditions,
                    emergencyContact = emergencyContact,
                    bloodType = bloodType
                ) ?: Patient(
                    name = name,
                    email = userEmail,
                    age = age.toIntOrNull() ?: 0,
                    conditions = conditions,
                    emergencyContact = emergencyContact,
                    bloodType = bloodType
                )

                // Save to Firestore and Room
                if (updatedProfile.id.isEmpty()) {
                    // Create new profile
                    val id = patientRepository.createPatient(updatedProfile)
                    _profile.value = updatedProfile.copy(id = id)
                } else {
                    // Update existing profile
                    patientRepository.updatePatient(updatedProfile)
                    _profile.value = updatedProfile
                }

                _updateSuccess.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update profile"
                _isLoading.value = false
            }
        }
    }

    /**
     * Set profile (used when navigating with profile data)
     */
    fun setProfile(patient: Patient) {
        _profile.value = patient
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear update success flag
     */
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
