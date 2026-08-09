package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>

    /** Синхронный доступ к uid текущей сессии (null — гость). Репозитории
     * данных используют это, чтобы решить, нужно ли отправлять запись в Firestore. */
    val currentUserId: String?

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun signOut()
}