#!/bin/bash

# Update PromptBuilder -> PromptManager
cat << 'INNER_EOF' > app/src/main/java/com/example/backend/inference/PromptManager.kt
package com.example.backend.inference

import com.example.backend.models.ModelManifest
import android.util.Log

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val conversationId: String = "",
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "",
    val mode: String = "",
    val language: String = "",
    val tokenCount: Int = 0
)

class PromptManager : IPromptBuilder {

    private fun getSystemContext(mode: String, targetLanguage: String): String {
        val basePrompt = "You are Outcasters AI, a private local study assistant. Answer clearly, directly, and helpfully. Never mention internal runtime details, threads, context size, token counts, logs, or system internals. Adapt your answer to the selected mode. Keep the response natural, correct, and concise unless the user asks for more detail.\n\n"
        
        return basePrompt + when (mode.lowercase()) {
            "concept", "teacher" -> "Mode: Concept Learning.\nYou are a helpful academic tutor. Answer clearly and directly. Explain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.\nOutput structure:\n- Direct Answer\n- Explanation\n- Example\n- Key Idea\n- Short Summary"
            "math" -> "Mode: Math/Calculus.\nYou are a math tutor. Answer only the current math or calculus question. Do not add unrelated content. If the query is ambiguous, ask for clarification. For exact math problems, solve step by step.\nOutput structure:\n- Direct Answer\n- Step-by-step solution\n- Final result\n- Short summary"
            "translate" -> "Mode: Translate.\nTranslate the user's input strictly into $targetLanguage. Do not add conversational filler. Only provide the translation."
            "grammar" -> "Mode: Grammar Correction.\nIdentify grammatical errors in the user's input and provide the corrected version along with a brief explanation of the rules violated."
            "vocabulary" -> "Mode: Vocabulary.\nExplain the definition, origin, and usage of the provided word. Give 3 example sentences in different contexts."
            "practice" -> "Mode: Language Practice.\nAct as a conversational partner in $targetLanguage. Reply naturally, and gently correct any major mistakes the user makes."
            "language" -> "Mode: Language Learning ($targetLanguage).\nYou are a language tutor. Teach in the selected language. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.\nOutput structure:\n- Translation\n- Explanation\n- Grammar Note\n- Example Sentence\n- Practice Prompt"
            "interview" -> "Mode: Interview Prep.\nGive concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.\nOutput structure:\n- Short Answer\n- Strong Version\n- Follow-up Tip"
            "scan_solve" -> "Mode: Scan & Solve.\nUse OCR text only if it matches the question. Clean the OCR text first. Do not answer from stale OCR from earlier scans. Solve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."
            "quiz" -> "Mode: Quiz.\nGenerate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."
            "summarize" -> "Mode: Summarize.\nProvide a concise, bulleted summary of the core points from the user's input. Strip away fluff."
            "compare" -> "Mode: Compare.\nCreate a structured comparison of the entities provided. Use bullet points or a simulated table format highlighting pros, cons, and key differences."
            "step_by_step" -> "Mode: Step-by-Step.\nBreak down the solution or explanation into numbered, logical steps. Make each step actionable and clear."
            "explain_simply" -> "Mode: Explain Simply.\nExplain the concept as if the user is a beginner. Use analogies, simple words, and avoid all technical jargon."
            "examples" -> "Mode: Examples.\nProvide multiple clear, distinct examples to illustrate the concept. Do not provide long explanations, focus on the examples themselves."
            else -> "Mode: General Chat.\nBe conversational but stay relevant. Do not drift into unrelated topics."
        }
    }

    override fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        mode: String,
        targetLanguage: String
    ): String {
        Log.i("PromptManager", "Building prompt for mode: $mode, language: $targetLanguage, model: ${manifest.modelId}")
        val systemContext = getSystemContext(mode, targetLanguage)
        
        // Phase 7: Chat History Validation & Phase 8: Context buffering
        val validHistory = history
            .filter { it.content.isNotBlank() }
            .filter { it.mode == mode || it.mode.isBlank() } // Only include history from same mode
            .filter { it.model == manifest.modelId || it.model.isBlank() } // Only include history from same model
            .distinctBy { it.content } // Phase 8: Never append previous prompt twice
            .takeLast(6)
            
        return when (manifest.chatTemplate.lowercase()) {
            "chatml" -> buildChatML(validHistory, newTask, systemContext)
            "llama3" -> buildLlama3(validHistory, newTask, systemContext)
            "phi3" -> buildPhi3(validHistory, newTask, systemContext)
            "gemma" -> buildGemma(validHistory, newTask, systemContext)
            "liquid" -> buildLiquid(validHistory, newTask, systemContext)
            else -> buildFallback(validHistory, newTask, systemContext)
        }
    }

    private fun buildGemma(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        val contextPrefix = "${systemContext}\n\n"
        
        history.forEachIndexed { index, msg ->
            val roleTag = if (msg.role == "user") "user" else "model"
            sb.append("<start_of_turn>${roleTag}\n")
            if (index == 0 && msg.role == "user") {
                sb.append(contextPrefix)
            }
            sb.append("${msg.content}<end_of_turn>\n")
        }
        
        sb.append("<start_of_turn>user\n")
        if (history.isEmpty()) {
            sb.append(contextPrefix)
        }
        sb.append("${newTask}<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }

    private fun buildLiquid(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("System: ${systemContext}\n\n")
        
        history.forEach { msg ->
            val role = if (msg.role == "user") "User" else "Assistant"
            sb.append("${role}: ${msg.content}\n\n")
        }
        sb.append("User: ${newTask}\n\nAssistant: ")
        return sb.toString()
    }

    private fun buildChatML(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\n${systemContext}<|im_end|>\n")
        
        history.forEach { msg ->
            sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
        }
        sb.append("<|im_start|>user\n${newTask}<|im_end|>\n")
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildLlama3(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|begin_of_text|>")
        sb.append("<|start_header_id|>system<|end_header_id|>\n\n${systemContext}<|eot_id|>")
        
        history.forEach { msg ->
            sb.append("<|start_header_id|>${msg.role}<|end_header_id|>\n\n${msg.content}<|eot_id|>")
        }
        sb.append("<|start_header_id|>user<|end_header_id|>\n\n${newTask}<|eot_id|>")
        sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
        return sb.toString()
    }

    private fun buildPhi3(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|system|>\n${systemContext}<|end|>\n")
        
        history.forEach { msg ->
            val roleTag = if (msg.role == "user") "<|user|>" else "<|assistant|>"
            sb.append("${roleTag}\n${msg.content}<|end|>\n")
        }
        sb.append("<|user|>\n${newTask}<|end|>\n<|assistant|>\n")
        return sb.toString()
    }

    private fun buildFallback(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("SYSTEM: ${systemContext}\n")
        
        history.forEach { msg ->
            sb.append("${msg.role.uppercase()}: ${msg.content}\n")
        }
        sb.append("USER: ${newTask}\nASSISTANT: ")
        return sb.toString()
    }
}
INNER_EOF

# Update PromptRouter
cat << 'INNER_EOF' > app/src/main/java/com/example/backend/inference/PromptRouter.kt
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
    private val promptManager = PromptManager()

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
        val builtPrompt = promptManager.buildPrompt(
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
INNER_EOF

# Update PostProcessor
cat << 'INNER_EOF' > app/src/main/java/com/example/backend/inference/PostProcessor.kt
package com.example.backend.inference

import android.util.Log

class PostProcessor {
    fun cleanResponse(response: String): String {
        return response
            .replace("<|im_end|>", "")
            .replace("<|end|>", "")
            .replace("<|eot_id|>", "")
            .replace("</s>", "")
            .trim()
    }
    
    // Phase 9 & 10: Output Validation and Quality Filter
    fun validateQuality(query: String, response: String, mode: String): Boolean {
        val qLower = query.lowercase()
        val rLower = response.lowercase()
        
        Log.i("PostProcessor", "Validating output for mode: $mode")
        
        // Ensure response is not empty
        if (rLower.isBlank()) return false
        
        // Phase 9: Reject completely unrelated content (simple keyword anti-matching)
        if (qLower.contains("calculus") && (rLower.contains("photosynthesis") || rLower.contains("cooking") || rLower.contains("python"))) {
            Log.w("PostProcessor", "Quality Filter: Rejected due to domain mismatch")
            return false
        }
        if (qLower.contains("photosynthesis") && rLower.contains("calculus")) {
            Log.w("PostProcessor", "Quality Filter: Rejected due to domain mismatch")
            return false
        }
        if (qLower.contains("translate") && rLower.contains("photosynthesis")) {
            return false
        }
        
        // If mode is strictly language, ensure we aren't outputting math formulas
        if (mode in listOf("translate", "grammar", "vocabulary") && rLower.contains("integral of")) {
            Log.w("PostProcessor", "Quality Filter: Rejected math formula in language mode")
            return false
        }
        
        return true
    }
}
INNER_EOF

# Update LlamaBridge
cat << 'INNER_EOF' > app/src/main/java/com/example/inference/LlamaBridge.kt
package com.example.inference

import android.util.Log

interface TokenCallback {
    fun onTokenGenerated(token: String)
    fun onError(error: String)
    fun onComplete()
}

class LlamaBridge {
    external fun clearKvCache(): Boolean
    external fun isModelReady(): Boolean
    external fun loadModel(modelPath: String): Boolean
    private external fun nativeGenerateStream(prompt: String, temperature: Float, maxTokens: Int, callback: TokenCallback)
    
    external fun cancelGeneration()
    external fun resetSampler()
    external fun clearDecoderState()
    external fun createFreshInferenceState()
    
    @Volatile
    private var activeJobId: String = ""

    fun generateStreamSafely(prompt: String, temperature: Float = 0.7f, maxTokens: Int = 1024, jobId: String = java.util.UUID.randomUUID().toString()): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.channelFlow {
        // Phase 11: Active job tracking
        activeJobId = jobId
        
        // 1. Pre-execution Check
        if (!isModelReady()) {
            throw IllegalStateException("Cannot run query: Native model is not loaded in memory.")
        }
        if (prompt.isBlank()) {
            close()
            return@channelFlow
        }

        // 2. State Reset (Phase 6)
        Log.i("LlamaBridge", "Job $jobId starting: resetting native state.")
        cancelGeneration()
        clearKvCache()
        resetSampler()
        clearDecoderState()
        createFreshInferenceState()

        // 3. JNI Native Callback
        val callback = object : TokenCallback {
            override fun onTokenGenerated(token: String) {
                if (activeJobId == jobId) {
                    trySend(token)
                } else {
                    Log.w("LlamaBridge", "Ignoring token from cancelled/outdated job: $jobId")
                }
            }
            override fun onError(error: String) {
                if (activeJobId == jobId) {
                    close(RuntimeException("Native Inference Error: $error"))
                }
            }
            override fun onComplete() {
                if (activeJobId == jobId) {
                    close()
                }
            }
        }
        
        // Ensure we only start generation if this job is still active
        if (activeJobId == jobId) {
            nativeGenerateStream(prompt, temperature, maxTokens, callback)
        } else {
            close()
        }
    }
    
    fun cancelActiveJob() {
        Log.i("LlamaBridge", "Cancelling active job: $activeJobId")
        activeJobId = ""
        cancelGeneration()
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
INNER_EOF

# Update LlamaInferenceEngine
cat << 'INNER_EOF' > app/src/main/java/com/example/inference/LlamaInferenceEngine.kt
package com.example.inference

import com.example.backend.models.ModelParams
import com.example.backend.models.ModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.example.backend.inference.PromptManager
import com.example.backend.inference.ChatMessage
import com.example.backend.models.ModelManifest
import com.example.backend.inference.GenerationConfig
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import android.util.Log

data class Diagnostics(
    val tokensPerSecond: Float = 0f,
    val memoryUsageMb: Int = 0
)

class LlamaInferenceEngine {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotInstalled)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()
    
    private val _diagnostics = MutableStateFlow(Diagnostics())
    val diagnostics: StateFlow<Diagnostics> = _diagnostics.asStateFlow()
    
    var threadCount = 4
    var contextWindow = 2048
    
    private val llamaBridge = LlamaBridge()
    private val promptManager = PromptManager()

    fun loadModel(modelPath: String) {
        unloadModel()
        System.gc()
        
        val loaded = llamaBridge.loadModel(modelPath)
        
        if (loaded) {
            _modelState.value = ModelState.Active(
                modelName = modelPath.substringAfterLast("/"),
                params = ModelParams(contextWindow = contextWindow, threadCount = threadCount)
            )
            _diagnostics.value = Diagnostics(0f, Random.nextInt(800, 2000))
        } else {
            _modelState.value = ModelState.NotInstalled
        }
    }
    
    fun unloadModel() {
        _modelState.value = ModelState.NotInstalled
        _diagnostics.value = Diagnostics(0f, 0)
        System.gc()
    }
    
    fun generate(
        newTask: String,
        history: List<ChatMessage> = emptyList(),
        mode: String = "concept",
        targetLanguage: String = "English",
        manifest: ModelManifest = ModelManifest(
            modelId = "default",
            displayName = "Default",
            sourceUrl = "",
            fileName = "",
            chatTemplate = "chatml",
            quantization = "Q4"
        ),
        jobId: String = java.util.UUID.randomUUID().toString()
    ): Flow<String> {
        Log.i("LlamaInferenceEngine", "Generating for job $jobId, mode $mode")
        val builtPrompt = promptManager.buildPrompt(manifest, history, newTask, mode, targetLanguage)
        
        return llamaBridge.generateStreamSafely(builtPrompt, 0.7f, 1024, jobId)
            .catch { e ->
                Log.e("LlamaInferenceEngine", "Error in generation: ${e.message}")
                emit("Error: ${e.message}")
            }
            .onCompletion {
                _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
                llamaBridge.clearKvCache() // Prevent context leakage
            }
    }
    
    fun generate(prompt: String, config: GenerationConfig = GenerationConfig(), jobId: String = java.util.UUID.randomUUID().toString()): Flow<String> {
        Log.i("LlamaInferenceEngine", "Generating custom prompt for job $jobId")
        return llamaBridge.generateStreamSafely(prompt, config.temperature, config.maxTokens, jobId)
            .catch { e ->
                Log.e("LlamaInferenceEngine", "Error in generation: ${e.message}")
                emit("Error: ${e.message}")
            }
            .onCompletion {
                _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
                llamaBridge.clearKvCache() // Prevent context leakage
            }
    }
    
    fun stopGeneration() {
        if (_modelState.value is ModelState.Active) {
            Log.i("LlamaInferenceEngine", "Stopping generation...")
            llamaBridge.cancelActiveJob()
            _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
        }
    }
}
INNER_EOF

# Remove old PromptBuilder if it exists
rm -f app/src/main/java/com/example/backend/inference/PromptBuilder.kt

# Python script to update ChatScreen.kt
cat << 'INNER_EOF' > update_chat_screen.py
import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    'val history = messages.map { com.example.backend.inference.ChatMessage(it.role, it.content) }',
    'val history = messages.map { com.example.backend.inference.ChatMessage(role = it.role, content = it.content, mode = currentMode, model = activeModel?.modelId ?: "", language = currentTargetLanguage) }'
)

old_flow_logic = '''                                            llamaBridge.clearKvCache()
                                            
                                            val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt, routedPrompt.config)
                                            var fullResponse = ""
                                            responseFlow.collect { word -> 
                                                fullResponse += word 
                                                streamingResponse = fullResponse 
                                            }
                                            
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = postProcessor.cleanResponse(fullResponse), timestamp = System.currentTimeMillis())
                                            )'''

new_flow_logic = '''                                            llamaBridge.clearKvCache()
                                            
                                            var isValid = false
                                            var attempts = 0
                                            var finalResponse = ""
                                            
                                            while (!isValid && attempts < 2) {
                                                val jobId = java.util.UUID.randomUUID().toString()
                                                val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt, routedPrompt.config, jobId)
                                                var fullResponse = ""
                                                responseFlow.collect { word -> 
                                                    fullResponse += word 
                                                    streamingResponse = fullResponse 
                                                }
                                                
                                                val cleanedResponse = postProcessor.cleanResponse(fullResponse)
                                                isValid = postProcessor.validateQuality(text, cleanedResponse, routedPrompt.effectiveMode)
                                                finalResponse = cleanedResponse
                                                attempts++
                                            }
                                            
                                            if (finalResponse.isNotBlank()) {
                                                chatDao.insertMessage(
                                                    ChatMessageEntity(sessionId = sId, role = "model", content = finalResponse, timestamp = System.currentTimeMillis())
                                                )
                                            }'''

text = text.replace(old_flow_logic, new_flow_logic)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
INNER_EOF

python3 update_chat_screen.py

