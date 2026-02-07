package com.cleansoft.duvoice.data.repository
}
    }
        }
            preferences[PreferencesKeys.IS_STEREO] = isStereo
        context.dataStore.edit { preferences ->
    suspend fun updateStereo(isStereo: Boolean) {

    }
        }
            preferences[PreferencesKeys.AUDIO_FORMAT] = format.name
        context.dataStore.edit { preferences ->
    suspend fun updateFormat(format: AudioFormat) {

    }
        }
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality.name
        context.dataStore.edit { preferences ->
    suspend fun updateQuality(quality: AudioQuality) {

    }
        )
            isStereo = preferences[PreferencesKeys.IS_STEREO] ?: false
            } ?: AudioFormat.AAC,
                AudioFormat.valueOf(it)
            format = preferences[PreferencesKeys.AUDIO_FORMAT]?.let {
            } ?: AudioQuality.MEDIUM,
                AudioQuality.valueOf(it)
            quality = preferences[PreferencesKeys.AUDIO_QUALITY]?.let {
        AudioSettings(
    val audioSettings: Flow<AudioSettings> = context.dataStore.data.map { preferences ->

    }
        val IS_STEREO = booleanPreferencesKey("is_stereo")
        val AUDIO_FORMAT = stringPreferencesKey("audio_format")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    private object PreferencesKeys {

class SettingsRepository(private val context: Context) {

)
    val isStereo: Boolean = false
    val format: AudioFormat = AudioFormat.AAC,
    val quality: AudioQuality = AudioQuality.MEDIUM,
data class AudioSettings(

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import com.cleansoft.duvoice.data.model.AudioQuality
import com.cleansoft.duvoice.data.model.AudioFormat
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.core.DataStore
import android.content.Context


