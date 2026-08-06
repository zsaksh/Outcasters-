package com.example.backend.rag

import java.io.File

/**
 * Fully local RAG system using semantic chunking, local embeddings (e.g., bge-small),
 * and SQLite for vector storage.
 */
interface LocalRagEngine {
    suspend fun ingestDocument(file: File, documentId: String): Result<Unit>
    suspend fun retrieveRelevantChunks(query: String, maxChunks: Int): List<RagChunk>
    suspend fun searchHybrid(query: String, maxChunks: Int): List<RagChunk> // Vector + BM25
}

data class RagChunk(
    val documentId: String,
    val content: String,
    val score: Float
)
