package com.example.backend.inference

import com.example.backend.models.ModelManifest

interface IPromptBuilder {
    fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        mode: String,
        targetLanguage: String
    ): String
}
