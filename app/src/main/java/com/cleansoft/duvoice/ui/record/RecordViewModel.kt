package com.cleansoft.duvoice.ui.record

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansoft.duvoice.data.model.AudioFormat
import com.cleansoft.duvoice.data.model.AudioQuality
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.repository.AudioSettings
import com.cleansoft.duvoice.data.repository.RecordingRepository
import com.cleansoft.duvoice.data.repository.SettingsRepository
import com.cleansoft.duvoice.service.AudioRecordService
import com.cleansoft.duvoice.util.audio.AudioRecorder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val recordingRepository = RecordingRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private var audioService: AudioRecordService? = null
    private var serviceBound = false

    // Estados
    private val _recordingState = MutableStateFlow<AudioRecorder.State>(AudioRecorder.State.IDLE)
    val recordingState: StateFlow<AudioRecorder.State> = _recordingState.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private val _selectedCategory = MutableStateFlow(Category.GENERAL)
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()

    private val _recordingName = MutableStateFlow("")
    val recordingName: StateFlow<String> = _recordingName.asStateFlow()

    // Configurações de áudio
    val audioSettings: StateFlow<AudioSettings> = settingsRepository.audioSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioSettings())

    // Evento para quando gravação é salva com sucesso
    private val _recordingSaved = MutableSharedFlow<Recording>()
    val recordingSaved: SharedFlow<Recording> = _recordingSaved.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private var currentOutputFile: File? = null

    val elapsedTimeFormatted: StateFlow<String> = _elapsedTime.map { millis ->
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "00:00")

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioRecordService.LocalBinder
            audioService = binder.getService()
            serviceBound = true

            // Observar estados do serviço
            viewModelScope.launch {
                audioService?.state?.collect { state ->
                    _recordingState.value = state
                }
            }
            viewModelScope.launch {
                audioService?.elapsedTime?.collect { time ->
                    _elapsedTime.value = time
                }
            }
            viewModelScope.launch {
                audioService?.amplitude?.collect { amp ->
                    _amplitude.value = amp
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    fun bindService(context: Context) {
        val intent = Intent(context, AudioRecordService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            val settings = audioSettings.value
            val fileName = generateFileName(settings.format)
            currentOutputFile = File(recordingRepository.getRecordingsDirectory(), fileName)

            // Gerar nome padrão
            _recordingName.value = "Gravação ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())}"

            val intent = Intent(context, AudioRecordService::class.java).apply {
                action = AudioRecordService.ACTION_START
                putExtra(AudioRecordService.EXTRA_OUTPUT_PATH, currentOutputFile?.absolutePath)
                putExtra(AudioRecordService.EXTRA_QUALITY, settings.quality.name)
                putExtra(AudioRecordService.EXTRA_IS_STEREO, settings.isStereo)
            }
            context.startForegroundService(intent)
        }
    }

    fun pauseRecording(context: Context) {
        val intent = Intent(context, AudioRecordService::class.java).apply {
            action = AudioRecordService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeRecording(context: Context) {
        val intent = Intent(context, AudioRecordService::class.java).apply {
            action = AudioRecordService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun stopRecording(context: Context) {
        viewModelScope.launch {
            val file = audioService?.stopRecording()

            if (file != null && file.exists() && file.length() > 44) { // 44 bytes é o header WAV
                val settings = audioSettings.value
                val duration = audioService?.getDuration() ?: 0L
                val sampleRate = audioService?.getSampleRate() ?: 44100
                val channels = audioService?.getChannels() ?: 1

                val recording = Recording(
                    name = _recordingName.value.ifBlank { "Gravação ${Date().time}" },
                    filePath = file.absolutePath,
                    duration = duration,
                    size = file.length(),
                    format = AudioFormat.WAV, // Por enquanto salvamos como WAV
                    category = _selectedCategory.value,
                    sampleRate = sampleRate,
                    channels = channels,
                    bitRate = settings.quality.bitRate
                )

                val id = recordingRepository.insertRecording(recording)
                _recordingSaved.emit(recording.copy(id = id))
            } else {
                _error.emit("Erro ao salvar gravação")
            }

            // Reset estados
            _elapsedTime.value = 0L
            _amplitude.value = 0
            _recordingName.value = ""
            currentOutputFile = null
        }
    }

    fun cancelRecording(context: Context) {
        audioService?.stopRecording()
        currentOutputFile?.delete()
        currentOutputFile = null

        _elapsedTime.value = 0L
        _amplitude.value = 0
        _recordingName.value = ""
        _recordingState.value = AudioRecorder.State.IDLE
    }

    fun setRecordingName(name: String) {
        _recordingName.value = name
    }

    fun setCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun updateQuality(quality: AudioQuality) {
        viewModelScope.launch {
            settingsRepository.updateQuality(quality)
        }
    }

    fun updateFormat(format: AudioFormat) {
        viewModelScope.launch {
            settingsRepository.updateFormat(format)
        }
    }

    fun updateStereo(isStereo: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateStereo(isStereo)
        }
    }

    private fun generateFileName(format: AudioFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "recording_$timestamp.wav" // Sempre salvamos como WAV primeiro
    }

    fun getAmplitudeHistory(): List<Int> {
        return audioService?.getAmplitudeHistory() ?: emptyList()
    }
}

