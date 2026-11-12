package com.example.sawtflow.data.local

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TranscriptHistoryRepository(
    private val dao: SavedTranscriptDao
) {

    val allTranscripts: Flow<List<SavedTranscript>> = dao.getAllTranscripts()

    suspend fun saveTranscript(
        transcriptText: String,
        audioFileName: String?,
        dialect: String,
        detectedIntent: String,
        extractedOrderId: String?,
        confidence: String,
        source: String = "Gemini 3.8 Flash"
    ): Long {
        val formatter = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
        val entry = SavedTranscript(
            audioFileName = audioFileName,
            transcript = transcriptText,
            dialect = dialect,
            detectedIntent = detectedIntent,
            extractedOrderId = extractedOrderId,
            confidence = confidence,
            source = source,
            timestamp = System.currentTimeMillis(),
            formattedTime = formatter.format(Date())
        )
        return dao.insertTranscript(entry)
    }

    suspend fun deleteTranscript(id: Long) {
        dao.deleteTranscriptById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllTranscripts()
    }
}
