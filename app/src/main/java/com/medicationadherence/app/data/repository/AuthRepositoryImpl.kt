package com.medicationadherence.app.data.repository

import com.medicationadherence.app.data.auth.FirebaseAuthDataSource
import com.medicationadherence.app.domain.model.AuthState
import com.medicationadherence.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Authentication repository implementation
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override val authState: Flow<AuthState>
        get() = firebaseAuthDataSource.authState

    override suspend fun signUp(email: String, password: String): Result<String> {
        return firebaseAuthDataSource.signUp(email, password)
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return firebaseAuthDataSource.signIn(email, password)
    }

    override suspend fun signOut(): Result<Unit> {
        return firebaseAuthDataSource.signOut()
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseAuthDataSource.getCurrentUserId()
    }

    override suspend fun isAuthenticated(): Boolean {
        return firebaseAuthDataSource.isAuthenticated()
    }
}
