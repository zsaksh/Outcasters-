package com.example.backend.ocr

import java.io.File

interface ILocalRagEngine {
    /**
     * On-device document chunking and vector retrieval.
     */
    suspend fun ingestDocument(file: File, documentId: String): Result<Unit>
    suspend fun retrieveRelevantChunks(query: String, maxChunks: Int): List<RagChunk>
    suspend fun deleteDocument(documentId: String)
}

data class RagChunk(
    val documentId: String,
    val content: String,
    val score: Float
)
