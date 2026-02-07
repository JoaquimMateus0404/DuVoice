package com.cleansoft.duvoice.data.model

enum class AudioFormat(val extension: String, val mimeType: String) {
    WAV("wav", "audio/wav"),
    AAC("aac", "audio/aac"),
    MP3("mp3", "audio/mpeg"),
    M4A("m4a", "audio/mp4")
}
