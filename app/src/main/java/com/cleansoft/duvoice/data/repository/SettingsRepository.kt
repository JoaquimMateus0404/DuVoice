package com.cleansoft.duvoice.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.cleansoft.duvoice.data.model.AudioFormat
import com.cleansoft.duvoice.data.model.AudioQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AudioSettings(
    val quality: AudioQuality = AudioQuality.MEDIUM,
    val format: AudioFormat = AudioFormat.AAC,
    val isStereo: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val AUDIO_FORMAT = stringPreferencesKey("audio_format")
        val IS_STEREO = booleanPreferencesKey("is_stereo")
    }

    val audioSettings: Flow<AudioSettings> = context.dataStore.data.map { preferences ->
        AudioSettings(
            quality = preferences[PreferencesKeys.AUDIO_QUALITY]?.let {
                AudioQuality.valueOf(it)
            } ?: AudioQuality.MEDIUM,
            format = preferences[PreferencesKeys.AUDIO_FORMAT]?.let {
                AudioFormat.valueOf(it)
            } ?: AudioFormat.AAC,
            isStereo = preferences[PreferencesKeys.IS_STEREO] ?: false
        )
    }

    suspend fun updateQuality(quality: AudioQuality) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality.name
        }
    }

    suspend fun updateFormat(format: AudioFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_FORMAT] = format.name
        }
    }

    suspend fun updateStereo(isStereo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_STEREO] = isStereo
        }
    }
}
