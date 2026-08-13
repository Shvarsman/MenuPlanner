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
    // Идентификаторы contextual tip'ов (маскот с подсказкой), которые
    // пользователь уже видел один раз — каждый показывается не более
    // одного раза за всё время использования приложения.
    val SEEN_TIPS = stringSetPreferencesKey("seen_tips")
}