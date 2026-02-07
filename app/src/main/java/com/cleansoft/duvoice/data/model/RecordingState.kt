package com.cleansoft.duvoice.data.model

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val elapsedTime: Long, val amplitude: Int) : RecordingState()
    data class Paused(val elapsedTime: Long) : RecordingState()
    object Stopped : RecordingState()
}

