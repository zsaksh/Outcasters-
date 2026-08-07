package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
    // Metadata tracking for inference pipeline
    val model: String = "",
    val mode: String = "",
    val language: String = "",
    val tokenCount: Int = 0
)

@Entity(tableName = "ocr_scans")
data class OcrScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val extractedText: String,
    val timestamp: Long
)

@Entity(tableName = "inference_metadata")
data class InferenceMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val latencyMs: Long,
    val tokenCount: Int,
    val memoryUsageMb: Int,
    val model: String,
    val timestamp: Long
)
