package com.cleansoft.duvoice.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.repository.RecordingRepository
import com.cleansoft.duvoice.util.audio.AudioPlayer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    val audioPlayer = AudioPlayer()

    private val _currentRecording = MutableStateFlow<Recording?>(null)
    val currentRecording: StateFlow<Recording?> = _currentRecording.asStateFlow()

    val playerState: StateFlow<AudioPlayer.State> = audioPlayer.state
    val currentPosition: StateFlow<Int> = audioPlayer.currentPosition
    val duration: StateFlow<Int> = audioPlayer.duration

    val currentPositionFormatted: StateFlow<String> = currentPosition.map { millis ->
        formatTime(millis)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "00:00")

    val durationFormatted: StateFlow<String> = duration.map { millis ->
        formatTime(millis)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "00:00")

    fun loadRecording(recordingId: Long) {
        viewModelScope.launch {
            val recording = repository.getRecordingById(recordingId)
            recording?.let {
                _currentRecording.value = it
                audioPlayer.prepare(it.filePath)
            }
        }
    }

    fun loadRecording(recording: Recording) {
        _currentRecording.value = recording
        audioPlayer.prepare(recording.filePath)
    }

    fun play() {
        audioPlayer.play()
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    fun seekTo(position: Int) {
        audioPlayer.seekTo(position)
    }

    fun skipForward(seconds: Int = 10) {
        val newPosition = (currentPosition.value + seconds * 1000).coerceAtMost(duration.value)
        audioPlayer.seekTo(newPosition)
    }

    fun skipBackward(seconds: Int = 10) {
        val newPosition = (currentPosition.value - seconds * 1000).coerceAtLeast(0)
        audioPlayer.seekTo(newPosition)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _currentRecording.value?.let { recording ->
                repository.toggleFavorite(recording.id, !recording.isFavorite)
                _currentRecording.value = recording.copy(isFavorite = !recording.isFavorite)
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

