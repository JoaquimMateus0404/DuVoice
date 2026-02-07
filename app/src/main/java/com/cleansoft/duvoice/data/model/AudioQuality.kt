package com.cleansoft.duvoice.data.model

enum class AudioQuality(
    val sampleRate: Int,
    val bitRate: Int,
    val displayName: String
) {
    LOW(22050, 64000, "Baixa"),
    MEDIUM(44100, 128000, "Média"),
    HIGH(48000, 256000, "Alta")
}

