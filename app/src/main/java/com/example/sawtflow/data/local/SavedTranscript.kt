package com.example.sawtflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_transcripts")
data class SavedTranscript(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioFileName: String? = null,
    val transcript: String,
    val dialect: String = "Gulf / Saudi",
    val detectedIntent: String = "Order status",
    val extractedOrderId: String? = null,
    val confidence: String = "99.4%",
    val source: String = "Gemini 3.8 Flash",
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String
)
