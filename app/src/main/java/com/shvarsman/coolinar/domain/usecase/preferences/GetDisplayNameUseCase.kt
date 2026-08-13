package com.shvarsman.coolinar.domain.usecase.preferences

import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDisplayNameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<String?> = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.displayName }
}