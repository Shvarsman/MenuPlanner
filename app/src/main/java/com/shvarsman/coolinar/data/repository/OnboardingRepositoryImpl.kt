package com.shvarsman.coolinar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.shvarsman.coolinar.data.local.datastore.AppPreferencesKeys
import com.shvarsman.coolinar.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    override val hasCompletedOnboarding: Flow<Boolean> =
        dataStore.data.map { it[AppPreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    override suspend fun setCompleted(startTour: Boolean) {
        dataStore.edit {
            it[AppPreferencesKeys.ONBOARDING_COMPLETED] = true
            it[AppPreferencesKeys.TOUR_PENDING] = startTour
        }
    }

    override val isTourPending: Flow<Boolean> =
        dataStore.data.map { it[AppPreferencesKeys.TOUR_PENDING] ?: false }

    override suspend fun consumeTourPending() {
        dataStore.edit { it[AppPreferencesKeys.TOUR_PENDING] = false }
    }
}