package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun signInAnonymously(): Result<Unit>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /** Привязывает email/пароль к текущему анонимному аккаунту, сохраняя его uid и данные. */
    suspend fun linkAnonymousWithEmail(email: String, password: String): Result<Unit>

    suspend fun signOut()
}