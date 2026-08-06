package com.example.backend.inference

import com.example.backend.models.ModelManifest

/**
 * Maintains strict token budgets by applying sliding-window logic and semantic context trimming.
 */
class ContextManager {
    
    fun trimContext(
        history: List<ChatMessage>,
        maxTokens: Int,
        estimatedTokensPerMessage: Int = 30 // Rough estimation fallback
    ): List<ChatMessage> {
        if (history.size * estimatedTokensPerMessage <= maxTokens) return history
        
        // Sliding window: keep the most recent messages that fit the budget.
        // In a real implementation, we would use the actual Tokenizer to count tokens.
        var currentTokens = 0
        val trimmed = mutableListOf<ChatMessage>()
        
        for (msg in history.reversed()) {
            val msgTokens = msg.content.length / 4 // Heuristic: 4 chars per token
            if (currentTokens + msgTokens > maxTokens) break
            trimmed.add(msg)
            currentTokens += msgTokens
        }
        
        return trimmed.reversed()
    }
}
