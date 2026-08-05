package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Имя пользователя — локальная настройка устройства, не привязана к Firebase.
 * Одинаково доступна и гостю, и вошедшему пользователю; переживает signOut().
 */
interface UserPreferencesRepository {
    val displayName: Flow<String?>
    suspend fun setDisplayName(name: String)
}