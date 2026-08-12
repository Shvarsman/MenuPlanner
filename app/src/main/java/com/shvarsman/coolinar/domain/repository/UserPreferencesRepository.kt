package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Имя пользователя — локальная настройка устройства, не привязана к Firebase.
 * Одинаково доступна и гостю, и вошедшему пользователю; переживает signOut().
 */
interface UserPreferencesRepository {
    val displayName: Flow<String?>
    suspend fun setDisplayName(name: String)

    /** Вид карточек на экранах со списком рецептов (фото/список) — хранится
     * как сырая строка (имя enum RecipeViewMode), а не сам enum, чтобы domain-
     * слой не зависел от presentation-модуля, где объявлен RecipeViewMode. */
    val recipeViewMode: Flow<String?>
    suspend fun setRecipeViewMode(mode: String)
}