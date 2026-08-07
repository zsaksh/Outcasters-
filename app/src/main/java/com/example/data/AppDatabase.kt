package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ModelEntity::class, 
        ChatSessionEntity::class, 
        ChatMessageEntity::class, 
        OcrScanEntity::class,
        ModelManifestEntity::class,
        InferenceMetadataEntity::class
    ], 
    version = 5, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun ocrDao(): OcrDao
    abstract fun modelManifestDao(): ModelManifestDao
    abstract fun inferenceMetadataDao(): InferenceMetadataDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Initial migration setup if needed
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN model TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN mode TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN language TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN tokenCount INTEGER NOT NULL DEFAULT 0")
                // Add index for conversation indexing
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_sessionId ON chat_messages(sessionId)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `inference_metadata` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `latencyMs` INTEGER NOT NULL, `tokenCount` INTEGER NOT NULL, `memoryUsageMb` INTEGER NOT NULL, `model` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
            }
        }
    }
}
