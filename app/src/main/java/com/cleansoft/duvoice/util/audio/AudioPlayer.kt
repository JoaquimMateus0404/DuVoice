package com.cleansoft.duvoice.util.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayer {

    companion object {
        private const val TAG = "AudioPlayer"
    }

    enum class State {
        IDLE, PLAYING, PAUSED, COMPLETED
    }

    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null

    private val _state = MutableStateFlow(State.IDLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _currentFilePath = MutableStateFlow<String?>(null)
    val currentFilePath: StateFlow<String?> = _currentFilePath.asStateFlow()

    fun prepare(filePath: String): Boolean {
        return try {
            release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()

                setOnCompletionListener {
                    _state.value = State.COMPLETED
                    _currentPosition.value = 0
                    stopProgressUpdates()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _state.value = State.IDLE
                    false
                }
            }

            _duration.value = mediaPlayer?.duration ?: 0
            _currentFilePath.value = filePath
            _state.value = State.IDLE
            _currentPosition.value = 0
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error preparing MediaPlayer", e)
            false
        }
    }

    fun play() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    _state.value = State.PLAYING
                    startProgressUpdates()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing", e)
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _state.value = State.PAUSED
                    stopProgressUpdates()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let { player ->
                player.stop()
                player.prepare()
                _state.value = State.IDLE
                _currentPosition.value = 0
                stopProgressUpdates()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping", e)
        }
    }

    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }

    fun togglePlayPause() {
        when (_state.value) {
            State.PLAYING -> pause()
            State.PAUSED, State.IDLE, State.COMPLETED -> play()
        }
    }

    fun release() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = State.IDLE
        _currentPosition.value = 0
        _duration.value = 0
        _currentFilePath.value = null
    }

    private fun startProgressUpdates() {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state.value == State.PLAYING) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(100)
            }
        }
    }

    private fun stopProgressUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    fun isPlaying(): Boolean = _state.value == State.PLAYING

    fun getDurationFormatted(): String {
        return formatTime(_duration.value)
    }

    fun getCurrentPositionFormatted(): String {
        return formatTime(_currentPosition.value)
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

