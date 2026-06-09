package com.nicolas.ttsapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nicolas.ttsapp.domain.model.TtsSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tts_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SPEECH_RATE   = floatPreferencesKey("speech_rate")
        val PITCH         = floatPreferencesKey("pitch")
        val VOICE_NAME    = stringPreferencesKey("voice_name")
        val VOLUME_GAIN   = floatPreferencesKey("volume_gain")
        val MAX_HISTORY   = intPreferencesKey("max_history_size")
    }

    val settings: Flow<TtsSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            TtsSettings(
                speechRate    = prefs[Keys.SPEECH_RATE]  ?: 1.0f,
                pitch         = prefs[Keys.PITCH]        ?: 1.0f,
                voiceName     = prefs[Keys.VOICE_NAME]   ?: "",
                volumeGain    = prefs[Keys.VOLUME_GAIN]  ?: 1.0f,
                maxHistorySize = prefs[Keys.MAX_HISTORY] ?: 200
            )
        }

    suspend fun updateSpeechRate(value: Float) {
        context.dataStore.edit { it[Keys.SPEECH_RATE] = value }
    }

    suspend fun updatePitch(value: Float) {
        context.dataStore.edit { it[Keys.PITCH] = value }
    }

    suspend fun updateVoiceName(name: String) {
        context.dataStore.edit { it[Keys.VOICE_NAME] = name }
    }

    suspend fun updateMaxHistory(size: Int) {
        context.dataStore.edit { it[Keys.MAX_HISTORY] = size }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
