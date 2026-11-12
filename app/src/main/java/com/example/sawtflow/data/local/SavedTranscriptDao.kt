package com.example.sawtflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTranscriptDao {

    @Query("SELECT * FROM saved_transcripts ORDER BY timestamp DESC")
    fun getAllTranscripts(): Flow<List<SavedTranscript>>

    @Query("SELECT * FROM saved_transcripts WHERE id = :id")
    suspend fun getTranscriptById(id: Long): SavedTranscript?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: SavedTranscript): Long

    @Query("DELETE FROM saved_transcripts WHERE id = :id")
    suspend fun deleteTranscriptById(id: Long)

    @Query("DELETE FROM saved_transcripts")
    suspend fun clearAllTranscripts()
}
