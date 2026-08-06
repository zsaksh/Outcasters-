package com.example.backend.download

interface IHuggingFaceClient {
    suspend fun resolveRepository(repoUrl: String): List<HfModelFile>
    suspend fun filterCompatibleArtifacts(files: List<HfModelFile>): List<HfModelFile>
}

data class HfModelFile(
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val quantization: String,
    val format: String
)
