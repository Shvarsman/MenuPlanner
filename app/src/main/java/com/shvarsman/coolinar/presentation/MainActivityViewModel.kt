package com.shvarsman.coolinar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository
) : ViewModel() {
    /** null — ещё не прочитали DataStore (держим сплэш-скрин), дальше true/false. */
    val hasCompletedOnboarding: StateFlow<Boolean?> = onboardingRepository.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}