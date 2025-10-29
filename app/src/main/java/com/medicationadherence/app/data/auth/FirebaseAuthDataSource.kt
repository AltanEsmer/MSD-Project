package com.medicationadherence.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Auth data source wrapper
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val _authState = MutableStateFlow<com.medicationadherence.app.domain.model.AuthState>(
        if (firebaseAuth.currentUser != null) {
            com.medicationadherence.app.domain.model.AuthState.Authenticated(
                firebaseAuth.currentUser!!.uid
            )
        } else {
            com.medicationadherence.app.domain.model.AuthState.Unauthenticated
        }
    )
    val authState: StateFlow<com.medicationadherence.app.domain.model.AuthState> = _authState.asStateFlow()

    init {
        // Listen for auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            _authState.value = if (auth.currentUser != null) {
                com.medicationadherence.app.domain.model.AuthState.Authenticated(
                    auth.currentUser!!.uid
                )
            } else {
                com.medicationadherence.app.domain.model.AuthState.Unauthenticated
            }
        }
    }

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Loading
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: ""
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Authenticated(userId)
            Result.success(userId)
        } catch (e: Exception) {
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Error(
                getErrorMessage(e)
            )
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Loading
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: ""
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Authenticated(userId)
            Result.success(userId)
        } catch (e: Exception) {
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Error(
                getErrorMessage(e)
            )
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            _authState.value = com.medicationadherence.app.domain.model.AuthState.Unauthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Get current Firebase user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Convert Firebase exception to user-friendly error message
     */
    private fun getErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Please enter a valid email address"
            "The password is too weak or the email is already in use." -> "Password is too weak or email already exists"
            "The given password is invalid. [ Password should be at least 6 characters ]" -> "Password must be at least 6 characters"
            "There is no user record corresponding to this identifier." -> "No account found with this email"
            "The password is invalid or the user does not have a password." -> "Incorrect password"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network error. Please check your connection"
            else -> exception.message ?: "An error occurred. Please try again"
        }
    }
}
