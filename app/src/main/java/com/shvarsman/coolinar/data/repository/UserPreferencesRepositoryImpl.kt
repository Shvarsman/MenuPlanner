package com.shvarsman.coolinar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.shvarsman.coolinar.data.local.datastore.AppPreferencesKeys
import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val displayName: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.DISPLAY_NAME] }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { it[AppPreferencesKeys.DISPLAY_NAME] = name }
    }
}