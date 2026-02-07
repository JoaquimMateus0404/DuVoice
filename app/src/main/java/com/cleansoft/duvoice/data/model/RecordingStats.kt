package com.cleansoft.duvoice.data.model

data class RecordingStats(
    val totalRecordings: Int = 0,
    val totalDurationMs: Long = 0,
    val totalSizeBytes: Long = 0,
    val recordingsThisWeek: Int = 0,
    val durationThisWeekMs: Long = 0,
    val averageDurationMs: Long = 0,
    val longestRecordingMs: Long = 0,
    val favoriteCount: Int = 0,
    val recordingsByCategory: Map<Category, Int> = emptyMap()
) {
    val totalHours: Double
        get() = totalDurationMs / 3600000.0

    val totalMinutes: Long
        get() = totalDurationMs / 60000

    val totalHoursFormatted: String
        get() {
            val hours = totalDurationMs / 3600000
            val minutes = (totalDurationMs % 3600000) / 60000
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }

    val durationThisWeekFormatted: String
        get() {
            val hours = durationThisWeekMs / 3600000
            val minutes = (durationThisWeekMs % 3600000) / 60000
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }

    val averageDurationFormatted: String
        get() {
            val minutes = averageDurationMs / 60000
            val seconds = (averageDurationMs % 60000) / 1000
            return "${minutes}m ${seconds}s"
        }

    val totalSizeFormatted: String
        get() {
            return when {
                totalSizeBytes >= 1_073_741_824 -> String.format("%.2f GB", totalSizeBytes / 1_073_741_824.0)
                totalSizeBytes >= 1_048_576 -> String.format("%.1f MB", totalSizeBytes / 1_048_576.0)
                totalSizeBytes >= 1024 -> String.format("%.0f KB", totalSizeBytes / 1024.0)
                else -> "$totalSizeBytes B"
            }
        }
}

