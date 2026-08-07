package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.backend.inference.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) {

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id.toString(),
                    conversationId = entity.sessionId.toString(),
                    role = entity.role,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    model = entity.model,
                    mode = entity.mode,
                    language = entity.language,
                    tokenCount = entity.tokenCount
                )
            }
        }
    }

    suspend fun insertMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        val entity = ChatMessageEntity(
            sessionId = message.conversationId.toLongOrNull() ?: 0L,
            role = message.role,
            content = message.content,
            timestamp = message.timestamp,
            model = message.model,
            mode = message.mode,
            language = message.language,
            tokenCount = message.tokenCount
        )
        chatDao.insertMessage(entity)
        return@withContext 1L
    }
    
    suspend fun createSession(title: String): Long = withContext(Dispatchers.IO) {
        val entity = ChatSessionEntity(title = title, timestamp = System.currentTimeMillis())
        chatDao.insertSession(entity)
    }
    
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()
    
    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteSession(sessionId)
    }
    
    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }
}
