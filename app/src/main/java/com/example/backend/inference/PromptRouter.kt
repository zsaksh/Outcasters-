package com.example.backend.inference

import com.example.backend.models.ModelManifest

data class RoutedPrompt(
    val builtPrompt: String,
    val useRetrieval: Boolean,
    val useOcr: Boolean,
    val useMathSolver: Boolean
)

class PromptRouter {
    private val promptBuilder = PromptBuilder()

    fun route(
        mode: String,
        targetLanguage: String,
        manifest: ModelManifest,
        history: List<ChatMessage>,
        newTask: String,
        ocrContext: String,
        retrievalContext: String
    ): RoutedPrompt {
        val lowerMode = mode.lowercase()
        val lowerQuery = newTask.lowercase()

        // 1. Semantic Check - classify query override
        val effectiveMode = when {
            lowerQuery.contains("calculus") || lowerQuery.contains("derivative") || lowerQuery.contains("integral") -> "math"
            else -> lowerMode
        }

        // 2. Retrieval Policy
        val useRetrieval = effectiveMode in listOf("chat", "concept", "interview")
        val useOcr = effectiveMode == "scan_solve"
        val useMathSolver = effectiveMode == "math" || effectiveMode == "scan_solve"

        // 3. Context Filtering
        // Clear previous context before generating new prompts as requested
        val filteredHistory = history.takeLast(2) // Only keeping the immediate previous turn
        
        // Ensure no stale OCR or retrieval leaks
        val activeOcr = if (useOcr) "\n[Context]: $ocrContext" else ""
        val activeRetrieval = if (useRetrieval && retrievalContext.isNotEmpty()) "\n[Context]: $retrievalContext" else ""
        
        val enhancedTask = newTask + activeOcr + activeRetrieval

        // 4. Prompt Building
        val builtPrompt = promptBuilder.buildPrompt(
            manifest = manifest,
            history = filteredHistory,
            newTask = enhancedTask,
            mode = effectiveMode,
            targetLanguage = targetLanguage
        )

        return RoutedPrompt(
            builtPrompt = builtPrompt,
            useRetrieval = useRetrieval,
            useOcr = useOcr,
            useMathSolver = useMathSolver
        )
    }
}
