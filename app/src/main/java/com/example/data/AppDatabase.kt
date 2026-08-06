package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ModelEntity::class, 
        ChatSessionEntity::class, 
        ChatMessageEntity::class, 
        OcrScanEntity::class,
        ModelManifestEntity::class
    ], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun ocrDao(): OcrDao
    abstract fun modelManifestDao(): ModelManifestDao
}
