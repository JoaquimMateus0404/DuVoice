package com.cleansoft.duvoice.data.repository

import android.content.Context
import com.cleansoft.duvoice.data.local.database.AppDatabase
import com.cleansoft.duvoice.data.local.entity.RecordingEntity
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class RecordingRepository(context: Context) {

    private val recordingDao = AppDatabase.getDatabase(context).recordingDao()
    private val appContext = context.applicationContext

    fun getAllRecordings(): Flow<List<Recording>> {
        return recordingDao.getAllRecordings().map { entities ->
            entities.map { it.toRecording() }
        }
    }

    fun getFavoriteRecordings(): Flow<List<Recording>> {
        return recordingDao.getFavoriteRecordings().map { entities ->
            entities.map { it.toRecording() }
        }
    }

    fun getRecordingsByCategory(category: Category): Flow<List<Recording>> {
        return recordingDao.getRecordingsByCategory(category.name).map { entities ->
            entities.map { it.toRecording() }
        }
    }

    fun searchRecordings(query: String): Flow<List<Recording>> {
        return recordingDao.searchRecordings(query).map { entities ->
            entities.map { it.toRecording() }
        }
    }

    fun getFilteredRecordings(
        category: Category?,
        favoritesOnly: Boolean,
        searchQuery: String?,
        sortOrder: SortOrder
    ): Flow<List<Recording>> {
        return recordingDao.getFilteredRecordings(
            category = category?.name,
            favoritesOnly = favoritesOnly,
            searchQuery = searchQuery,
            sortBy = sortOrder.name
        ).map { entities ->
            entities.map { it.toRecording() }
        }
    }

    suspend fun getRecordingById(id: Long): Recording? {
        return recordingDao.getRecordingById(id)?.toRecording()
    }

    suspend fun insertRecording(recording: Recording): Long {
        return recordingDao.insertRecording(RecordingEntity.fromRecording(recording))
    }

    suspend fun updateRecording(recording: Recording) {
        recordingDao.updateRecording(RecordingEntity.fromRecording(recording))
    }

    suspend fun deleteRecording(recording: Recording) {
        // Apagar ficheiro do storage
        val file = File(recording.filePath)
        if (file.exists()) {
            file.delete()
        }
        // Apagar do banco de dados
        recordingDao.deleteRecording(RecordingEntity.fromRecording(recording))
    }

    suspend fun deleteRecordingById(id: Long) {
        val recording = recordingDao.getRecordingById(id)
        if (recording != null) {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
            recordingDao.deleteRecordingById(id)
        }
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        recordingDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun renameRecording(id: Long, newName: String) {
        recordingDao.renameRecording(id, newName)
    }

    suspend fun updateCategory(id: Long, category: Category) {
        recordingDao.updateCategory(id, category.name)
    }

    fun getRecordingsDirectory(): File {
        val dir = File(appContext.getExternalFilesDir(null), "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}

