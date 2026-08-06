package com.example.backend.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val AUTO_ACTIVATE_MODELS = booleanPreferencesKey("auto_activate_models")
        val OFFLINE_ONLY_MODE = booleanPreferencesKey("offline_only_mode")
        val MEMORY_SAVER_MODE = booleanPreferencesKey("memory_saver_mode")
        val REAL_TIME_WEB_SEARCH = booleanPreferencesKey("real_time_web_search")
    }

    val autoActivateModels: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_ACTIVATE_MODELS] ?: true
    }

    val offlineOnlyMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[OFFLINE_ONLY_MODE] ?: false
    }

    val memorySaverMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MEMORY_SAVER_MODE] ?: false
    }

    val realTimeWebSearch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REAL_TIME_WEB_SEARCH] ?: true
    }

    suspend fun setAutoActivateModels(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_ACTIVATE_MODELS] = enabled
        }
    }

    suspend fun setOfflineOnlyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OFFLINE_ONLY_MODE] = enabled
        }
    }

    suspend fun setMemorySaverMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MEMORY_SAVER_MODE] = enabled
        }
    }

    suspend fun setRealTimeWebSearch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REAL_TIME_WEB_SEARCH] = enabled
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
