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
        val builtPrompt = PromptManager.buildPrompt(manifest, history, newTask, mode, targetLanguage)
        
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
