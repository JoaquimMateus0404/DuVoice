package com.cleansoft.duvoice.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansoft.duvoice.data.model.AudioFormat
import com.cleansoft.duvoice.data.model.AudioQuality
import com.cleansoft.duvoice.data.repository.AudioSettings
import com.cleansoft.duvoice.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val audioSettings: StateFlow<AudioSettings> = repository.audioSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioSettings())

    fun updateQuality(quality: AudioQuality) {
        viewModelScope.launch {
            repository.updateQuality(quality)
        }
    }

    fun updateFormat(format: AudioFormat) {
        viewModelScope.launch {
            repository.updateFormat(format)
        }
    }

    fun updateStereo(isStereo: Boolean) {
        viewModelScope.launch {
            repository.updateStereo(isStereo)
        }
    }
}

