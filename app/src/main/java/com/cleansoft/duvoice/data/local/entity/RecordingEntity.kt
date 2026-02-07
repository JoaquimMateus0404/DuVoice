package com.cleansoft.duvoice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cleansoft.duvoice.data.model.AudioFormat
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.Recording
import java.util.Date

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val filePath: String,
    val duration: Long,
    val size: Long,
    val format: String,
    val category: String,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val sampleRate: Int,
    val channels: Int,
    val bitRate: Int
) {
    fun toRecording(): Recording {
        return Recording(
            id = id,
            name = name,
            filePath = filePath,
            duration = duration,
            size = size,
            format = AudioFormat.valueOf(format),
            category = Category.valueOf(category),
            isFavorite = isFavorite,
            createdAt = Date(createdAt),
            sampleRate = sampleRate,
            channels = channels,
            bitRate = bitRate
        )
    }

    companion object {
        fun fromRecording(recording: Recording): RecordingEntity {
            return RecordingEntity(
                id = recording.id,
                name = recording.name,
                filePath = recording.filePath,
                duration = recording.duration,
                size = recording.size,
                format = recording.format.name,
                category = recording.category.name,
                isFavorite = recording.isFavorite,
                createdAt = recording.createdAt.time,
                sampleRate = recording.sampleRate,
                channels = recording.channels,
                bitRate = recording.bitRate
            )
        }
    }
}
