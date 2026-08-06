package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_manifests")
data class ModelManifestEntity(
    @PrimaryKey val modelId: String,
    val displayName: String,
    val repoId: String?,
    val sourceUrl: String?,
    val directFileUrl: String?,
    val fileName: String,
    val format: String,
    val quantization: String,
    val checksum: String?,
    val fileSizeBytes: Long,
    val contextLength: Int,
    val tokenizer: String,
    val chatTemplate: String,
    val runtimeAdapter: String,
    val languageSupport: List<String>,
    val taskSupport: List<String>,
    val ramEstimateMB: Int,
    val storageEstimateMB: Int,
    val performanceTier: String,
    val deviceClass: String,
    val installStatus: String,
    val active: Boolean,
    val lastVerifiedAt: Long?,
    val compatibilityFlags: List<String>,
    val errorState: String?
)
