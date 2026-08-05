package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    suspend fun signOut()
}