package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Insert
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentMessage(): Flow<ChatMessageEntity?>

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface OcrDao {
    @Query("SELECT * FROM ocr_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<OcrScanEntity>>

    @Insert
    suspend fun insertScan(scan: OcrScanEntity): Long

    @Query("DELETE FROM ocr_scans WHERE id = :scanId")
    suspend fun deleteScan(scanId: Long)
}
