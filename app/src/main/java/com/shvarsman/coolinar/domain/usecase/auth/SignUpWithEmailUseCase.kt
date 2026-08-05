package com.shvarsman.coolinar.domain.usecase.auth

import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Регистрация с автоподстановкой имени по умолчанию = email.
 * Имя — локальная настройка (DataStore), поэтому её нельзя сохранить внутри
 * AuthRepository — оркестрируем на уровне use case.
 */
class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        val result = authRepository.signUpWithEmail(email, password)
        if (result.isSuccess) {
            userPreferencesRepository.setDisplayName(email)
        }
        return result
    }
}