package com.example.backend.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelManifest(
    @PrimaryKey val modelId: String,
    val displayName: String,
    val sourceUrl: String,
    val fileName: String,
    val format: String = "GGUF",
    val quantization: String = "",
    val checksum: String = "",
    val fileSizeBytes: Long = 0L,
    val estimatedRamMB: Int = 0,
    val estimatedStorageMB: Int = 0,
    val tokenizerInfo: String = "",
    val chatTemplate: String = "",
    val compatibilityFlag: String = "",
    val installStatus: String = "not_installed", // not_installed, downloading, paused, verifying, ready, failed, corrupted, unsupported
    val downloadProgress: Int = 0,
    val activeStatus: Boolean = false,
    val errorState: String = "",
    val lastUsedTime: Long = 0L,
    val lastVerifiedTime: Long = 0L
)
