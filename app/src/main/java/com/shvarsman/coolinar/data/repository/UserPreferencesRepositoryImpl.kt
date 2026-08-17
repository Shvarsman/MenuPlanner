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

    override val recipeViewMode: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.RECIPE_VIEW_MODE] }

    override suspend fun setRecipeViewMode(mode: String) {
        dataStore.edit { it[AppPreferencesKeys.RECIPE_VIEW_MODE] = mode }
    }

    override val fridgeSortOption: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.FRIDGE_SORT_OPTION] }

    override suspend fun setFridgeSortOption(option: String) {
        dataStore.edit { it[AppPreferencesKeys.FRIDGE_SORT_OPTION] = option }
    }

    override val fridgeGroupByCategory: Flow<Boolean> =
        dataStore.data.map { it[AppPreferencesKeys.FRIDGE_GROUP_BY_CATEGORY] ?: false }

    override suspend fun setFridgeGroupByCategory(value: Boolean) {
        dataStore.edit { it[AppPreferencesKeys.FRIDGE_GROUP_BY_CATEGORY] = value }
    }

    override val shoppingSortOption: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.SHOPPING_SORT_OPTION] }

    override suspend fun setShoppingSortOption(option: String) {
        dataStore.edit { it[AppPreferencesKeys.SHOPPING_SORT_OPTION] = option }
    }

    override val recipeSortOption: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.RECIPE_SORT_OPTION] }

    override suspend fun setRecipeSortOption(option: String) {
        dataStore.edit { it[AppPreferencesKeys.RECIPE_SORT_OPTION] = option }
    }

    override val recipeGroupingOption: Flow<String?> =
        dataStore.data.map { it[AppPreferencesKeys.RECIPE_GROUPING_OPTION] }

    override suspend fun setRecipeGroupingOption(option: String) {
        dataStore.edit { it[AppPreferencesKeys.RECIPE_GROUPING_OPTION] = option }
    }
}