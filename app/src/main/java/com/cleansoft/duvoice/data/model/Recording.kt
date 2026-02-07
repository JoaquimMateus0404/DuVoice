package com.cleansoft.duvoice.data.model

import java.util.Date

data class Recording(
    val id: Long = 0,
    val name: String,
    val filePath: String,
    val duration: Long, // em milissegundos
    val size: Long, // em bytes
    val format: AudioFormat,
    val category: Category = Category.GENERAL,
    val isFavorite: Boolean = false,
    val createdAt: Date = Date(),
    val sampleRate: Int = 44100,
    val channels: Int = 1, // 1 = mono, 2 = stereo
    val bitRate: Int = 128000
) {
    val durationFormatted: String
        get() {
            val totalSeconds = duration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeFormatted: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0

            return when {
                mb >= 1 -> String.format("%.1f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }
}

