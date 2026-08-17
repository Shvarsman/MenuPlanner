package com.shvarsman.coolinar.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

object AppPreferencesKeys {
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val RECIPE_VIEW_MODE = stringPreferencesKey("recipe_view_mode")
    val SEEN_TIPS = stringSetPreferencesKey("seen_tips")
    val TOUR_PENDING = booleanPreferencesKey("tour_pending")
    val TIPS_ENABLED = booleanPreferencesKey("tips_enabled")
    val FRIDGE_SORT_OPTION = stringPreferencesKey("fridge_sort_option")
    val FRIDGE_GROUP_BY_CATEGORY = booleanPreferencesKey("fridge_group_by_category")
    val SHOPPING_SORT_OPTION = stringPreferencesKey("shopping_sort_option")
    val RECIPE_SORT_OPTION = stringPreferencesKey("recipe_sort_option")
    val RECIPE_GROUPING_OPTION = stringPreferencesKey("recipe_grouping_option")
}