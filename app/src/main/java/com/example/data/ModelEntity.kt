package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val isDownloaded: Boolean
)
