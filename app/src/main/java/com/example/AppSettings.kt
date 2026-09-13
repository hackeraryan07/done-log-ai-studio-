package com.example

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

object AppSettings {
    val PIN_KEY = stringPreferencesKey("app_pin")
    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    fun getPin(context: Context) = context.dataStore.data.map { prefs ->
        prefs[PIN_KEY]
    }

    suspend fun setPin(context: Context, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
        }
    }

    fun getThemeMode(context: Context) = context.dataStore.data.map { prefs ->
        val modeStr = prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }
}
