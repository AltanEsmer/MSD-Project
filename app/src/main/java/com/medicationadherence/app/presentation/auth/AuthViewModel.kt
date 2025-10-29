package com.medicationadherence.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.domain.model.AuthState
import com.medicationadherence.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        if (!isEmailValid(email)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.signIn(email.trim(), password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Sign in failed. Please try again."
                    )
                }
        }
    }

    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (!isEmailValid(email)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password must be at least 6 characters"
            )
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.signUp(email.trim(), password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Sign up failed. Please try again."
                    )
                }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = AuthUiState()
    }

    /**
     * Validate email format
     */
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * UI state for authentication screens
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
