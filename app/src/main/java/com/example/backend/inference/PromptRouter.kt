package com.example.backend.inference

import com.example.backend.models.ModelManifest
import android.util.Log

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024,
    val stopTokens: List<String> = listOf("<|im_end|>", "<|end|>", "<|eot_id|>", "</s>"),
    val formatting: String = "markdown"
)

data class RoutedPrompt(
    val builtPrompt: String,
    val useRetrieval: Boolean,
    val useOcr: Boolean,
    val useMathSolver: Boolean,
    val config: GenerationConfig = GenerationConfig(),
    val effectiveMode: String = "chat"
)

class PromptRouter {
    // PromptManager is now an object

    fun route(
        mode: String,
        targetLanguage: String,
        manifest: ModelManifest,
        history: List<ChatMessage>,
        newTask: String,
        ocrContext: String,
        retrievalContext: String,
        isNewSession: Boolean = true
    ): RoutedPrompt {
        val activeOcr = if (ocrContext.isNotBlank()) "\n[Context]: $ocrContext" else ""
        
        val enhancedTask = newTask + activeOcr

        val builtPrompt = PromptManager.buildPrompt(
            manifest = manifest,
            history = history,
            newTask = enhancedTask,
            mode = "chat",
            targetLanguage = targetLanguage,
            isNewSession = isNewSession
        )

        return RoutedPrompt(
            builtPrompt = builtPrompt,
            useRetrieval = false,
            useOcr = false,
            useMathSolver = false,
            config = GenerationConfig(temperature = 0.7f, maxTokens = 1024),
            effectiveMode = "chat"
        )
    }
}
