package com.shvarsman.coolinar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.shvarsman.coolinar.data.local.datastore.AppPreferencesKeys
import com.shvarsman.coolinar.domain.repository.TipsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TipsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TipsRepository {

    override fun isTipSeen(tipId: String): Flow<Boolean> =
        dataStore.data.map { prefs -> tipId in (prefs[AppPreferencesKeys.SEEN_TIPS] ?: emptySet()) }

    override suspend fun markTipSeen(tipId: String) {
        dataStore.edit { prefs ->
            val current = prefs[AppPreferencesKeys.SEEN_TIPS] ?: emptySet()
            prefs[AppPreferencesKeys.SEEN_TIPS] = current + tipId
        }
    }
}