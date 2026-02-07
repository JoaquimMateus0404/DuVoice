package com.cleansoft.duvoice.data.local.dao

import androidx.room.*
import com.cleansoft.duvoice.data.local.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): RecordingEntity?

    @Query("SELECT * FROM recordings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecordings(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE category = :category ORDER BY createdAt DESC")
    fun getRecordingsByCategory(category: String): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecordings(query: String): Flow<List<RecordingEntity>>

    @Query("""
        SELECT * FROM recordings 
        WHERE (:category IS NULL OR category = :category)
        AND (:favoritesOnly = 0 OR isFavorite = 1)
        AND (:searchQuery IS NULL OR name LIKE '%' || :searchQuery || '%')
        ORDER BY 
            CASE WHEN :sortBy = 'DATE_DESC' THEN createdAt END DESC,
            CASE WHEN :sortBy = 'DATE_ASC' THEN createdAt END ASC,
            CASE WHEN :sortBy = 'NAME_ASC' THEN name END ASC,
            CASE WHEN :sortBy = 'NAME_DESC' THEN name END DESC,
            CASE WHEN :sortBy = 'DURATION_DESC' THEN duration END DESC,
            CASE WHEN :sortBy = 'DURATION_ASC' THEN duration END ASC
    """)
    fun getFilteredRecordings(
        category: String?,
        favoritesOnly: Boolean,
        searchQuery: String?,
        sortBy: String
    ): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Update
    suspend fun updateRecording(recording: RecordingEntity)

    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)

    @Query("UPDATE recordings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE recordings SET name = :newName WHERE id = :id")
    suspend fun renameRecording(id: Long, newName: String)

    @Query("UPDATE recordings SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)
}

