package com.alphaxk.autotap

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("autotap")

class Prefs(private val ctx: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val WATCH_PATTERN = stringPreferencesKey("watch_pattern")
        val BUTTON_TEXT = stringPreferencesKey("button_text")
        val CONFIRM_BUTTON_TEXT = stringPreferencesKey("confirm_button_text")
        val PACKAGE_FILTER = stringPreferencesKey("package_filter")
        val COOLDOWN_MS = longPreferencesKey("cooldown_ms")
    }

    val configFlow: Flow<TriggerConfig> = ctx.dataStore.data.map { p ->
        TriggerConfig(
            enabled = p[Keys.ENABLED] ?: false,
            watchPattern = p[Keys.WATCH_PATTERN] ?: "",
            buttonText = p[Keys.BUTTON_TEXT] ?: "حجز فترة الدوام",
            confirmButtonText = p[Keys.CONFIRM_BUTTON_TEXT] ?: "",
            packageFilter = p[Keys.PACKAGE_FILTER] ?: "",
            cooldownMs = p[Keys.COOLDOWN_MS] ?: 2000L
        )
    }

    suspend fun save(config: TriggerConfig) {
        ctx.dataStore.edit { p ->
            p[Keys.ENABLED] = config.enabled
            p[Keys.WATCH_PATTERN] = config.watchPattern
            p[Keys.BUTTON_TEXT] = config.buttonText
            p[Keys.CONFIRM_BUTTON_TEXT] = config.confirmButtonText
            p[Keys.PACKAGE_FILTER] = config.packageFilter
            p[Keys.COOLDOWN_MS] = config.cooldownMs
        }
    }
}
