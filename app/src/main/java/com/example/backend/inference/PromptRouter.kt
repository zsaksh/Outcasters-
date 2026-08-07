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
        retrievalContext: String
    ): RoutedPrompt {
        val lowerMode = mode.lowercase()
        val lowerQuery = newTask.lowercase()

        // 1. Semantic Check - classify query override
        val effectiveMode = when {
            lowerQuery.contains("calculus") || lowerQuery.contains("derivative") || lowerQuery.contains("integral") -> "math"
            lowerQuery.contains("photosynthesis") -> "concept"
            lowerQuery.contains("translate") -> "translate"
            lowerQuery.contains("grammar") -> "grammar"
            lowerQuery.contains("vocabulary") -> "vocabulary"
            lowerQuery.contains("practice") -> "practice"
            lowerQuery.contains("interview") -> "interview"
            lowerQuery.contains("quiz") -> "quiz"
            lowerQuery.contains("summarize") -> "summarize"
            lowerQuery.contains("compare") -> "compare"
            lowerQuery.contains("step by step") -> "step_by_step"
            lowerQuery.contains("explain simply") -> "explain_simply"
            lowerQuery.contains("examples") -> "examples"
            lowerQuery.contains("gravity") -> "concept"
            lowerQuery.contains("what is ai") -> "concept"
            else -> lowerMode
        }

        Log.i("PromptRouter", "Routing query: '$newTask' -> Mode: $effectiveMode")

        // 2. Retrieval Policy
        val useRetrieval = effectiveMode in listOf("chat", "concept", "interview")
        val useOcr = effectiveMode == "scan_solve"
        val useMathSolver = effectiveMode == "math" || effectiveMode == "scan_solve"

        // 3. Config mapping based on mode
        val config = when (effectiveMode) {
            "math", "scan_solve" -> GenerationConfig(temperature = 0.1f, maxTokens = 2048)
            "translate", "grammar" -> GenerationConfig(temperature = 0.2f, maxTokens = 512)
            "concept", "teacher", "explain_simply" -> GenerationConfig(temperature = 0.5f, maxTokens = 1500)
            "interview", "practice" -> GenerationConfig(temperature = 0.7f, maxTokens = 1024)
            "vocabulary", "quiz" -> GenerationConfig(temperature = 0.6f, maxTokens = 800)
            "summarize" -> GenerationConfig(temperature = 0.3f, maxTokens = 1024)
            "compare", "step_by_step", "examples" -> GenerationConfig(temperature = 0.4f, maxTokens = 1200)
            else -> GenerationConfig(temperature = 0.7f, maxTokens = 1024)
        }

        val activeOcr = if (useOcr && ocrContext.isNotBlank()) "\n[Context]: $ocrContext" else ""
        val activeRetrieval = if (useRetrieval && retrievalContext.isNotBlank()) "\n[Context]: $retrievalContext" else ""
        
        val enhancedTask = newTask + activeOcr + activeRetrieval

        // 4. Prompt Building
        val builtPrompt = PromptManager.buildPrompt(
            manifest = manifest,
            history = history,
            newTask = enhancedTask,
            mode = effectiveMode,
            targetLanguage = targetLanguage
        )

        return RoutedPrompt(
            builtPrompt = builtPrompt,
            useRetrieval = useRetrieval,
            useOcr = useOcr,
            useMathSolver = useMathSolver,
            config = config,
            effectiveMode = effectiveMode
        )
    }
}
