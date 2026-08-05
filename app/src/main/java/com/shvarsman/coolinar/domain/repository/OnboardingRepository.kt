package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setCompleted()
}