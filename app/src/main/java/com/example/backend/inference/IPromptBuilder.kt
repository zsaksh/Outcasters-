package com.example.backend.inference

import com.example.backend.models.ModelManifest

interface IPromptBuilder {
    /**
     * Dynamic system prompt injection and chat templating.
     */
    fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        systemContext: String? = null
    ): String
}
