package com.medicationadherence.app.presentation.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.domain.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for profile management
 */
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _profile = MutableStateFlow<Patient?>(null)
    val profile: StateFlow<Patient?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    /**
     * Load profile from SharedPreferences
     */
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Load from Firestore in the future
                // For now, we'll use the profile data passed from navigation
                _isLoading.value = false
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
                val updatedProfile = currentProfile?.copy(
                    name = name,
                    age = age.toIntOrNull() ?: currentProfile.age,
                    conditions = conditions,
                    emergencyContact = emergencyContact,
                    bloodType = bloodType
                ) ?: Patient(
                    name = name,
                    email = "",
                    age = age.toIntOrNull() ?: 0,
                    conditions = conditions,
                    emergencyContact = emergencyContact,
                    bloodType = bloodType
                )

                _profile.value = updatedProfile
                _updateSuccess.value = true
                _isLoading.value = false

                // TODO: Save to Firestore in the future
                // For now, save to SharedPreferences or keep in memory
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
