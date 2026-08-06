package com.example.backend.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelManifest(
    @PrimaryKey val modelId: String,
    val displayName: String,
    val repoId: String,
    val sourceUrl: String,
    val fileName: String,
    val format: String = "GGUF",
    val quantization: String,
    val fileSizeBytes: Long,
    val checksum: String = "",
    val tokenizerType: String = "",
    val chatTemplate: String = "",
    val contextLength: Int = 4096,
    val ramEstimateMB: Int = 0,
    val storageEstimateMB: Int = 0,
    val deviceRecommendationTier: String = "Standard",
    val downloadStatus: String = "not_installed", // not_installed, downloading, downloaded
    val installStatus: String = "pending", // pending, verifying, ready, corrupted, unsupported
    val activeStatus: Boolean = false,
    val errorState: String = "",
    val lastVerifiedAt: Long = 0L,
    val lastUsedAt: Long = 0L
)
