package com.example.backend.manager

import com.example.backend.inference.ChatMessage
import kotlinx.coroutines.flow.Flow

interface IConversationManager {
    fun getConversations(): Flow<List<ConversationMetadata>>
    fun getMessages(conversationId: Long): Flow<List<ChatMessage>>
    
    suspend fun createConversation(title: String): Long
    suspend fun addMessage(conversationId: Long, message: ChatMessage)
    suspend fun deleteConversation(conversationId: Long)
    suspend fun clearHistory(conversationId: Long)
}

data class ConversationMetadata(
    val id: Long,
    val title: String,
    val lastUpdated: Long
)
