package com.cleansoft.duvoice.data.local.entity
}
    }
        }
            )
                bitRate = recording.bitRate
                channels = recording.channels,
                sampleRate = recording.sampleRate,
                createdAt = recording.createdAt.time,
                isFavorite = recording.isFavorite,
                category = recording.category.name,
                format = recording.format.name,
                size = recording.size,
                duration = recording.duration,
                filePath = recording.filePath,
                name = recording.name,
                id = recording.id,
            return RecordingEntity(
        fun fromRecording(recording: Recording): RecordingEntity {
    companion object {

    }
        )
            bitRate = bitRate
            channels = channels,
            sampleRate = sampleRate,
            createdAt = Date(createdAt),
            isFavorite = isFavorite,
            category = Category.valueOf(category),
            format = AudioFormat.valueOf(format),
            size = size,
            duration = duration,
            filePath = filePath,
            name = name,
            id = id,
        return Recording(
    fun toRecording(): Recording {
) {
    val bitRate: Int
    val channels: Int,
    val sampleRate: Int,
    val createdAt: Long,
    val isFavorite: Boolean = false,
    val category: String,
    val format: String,
    val size: Long,
    val duration: Long,
    val filePath: String,
    val name: String,
    val id: Long = 0,
    @PrimaryKey(autoGenerate = true)
data class RecordingEntity(
@Entity(tableName = "recordings")

import java.util.Date
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.AudioFormat
import androidx.room.PrimaryKey
import androidx.room.Entity


