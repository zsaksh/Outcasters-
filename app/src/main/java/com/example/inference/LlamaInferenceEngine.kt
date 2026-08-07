package com.example.inference

import android.content.Context
import android.util.Log
import com.example.backend.inference.ChatMessage
import com.example.backend.inference.GenerationConfig
import com.example.backend.inference.PromptManager
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelParams
import com.example.backend.models.ModelState
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

data class Diagnostics(val tokensPerSecond: Float = 0f, val memoryUsageMb: Int = 0)

class LlamaInferenceEngine(private val context: Context) {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotInstalled)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private val _diagnostics = MutableStateFlow(Diagnostics())
    val diagnostics: StateFlow<Diagnostics> = _diagnostics.asStateFlow()

    var threadCount = 4
    var contextWindow = 2048
    private val inferenceMutex = Mutex()

    private var llmInference: LlmInference? = null
    private var currentOptions: LlmInference.LlmInferenceOptions? = null
    private var lastSessionId: String? = null
    
    // Channel for streaming responses
    private var currentGenerationChannel: Channel<String>? = null

    fun loadModel(modelFileName: String) {
        unloadModel()
        System.gc()
        _modelState.value = com.example.backend.models.ModelState.Loading

        try {
            val modelsDir = File(context.filesDir, "models")
            val modelFile = File(modelsDir, modelFileName)
            
            if (!modelFile.exists()) {
                Log.e("LlamaInferenceEngine", "Model file not found: ${modelFile.absolutePath}")
                _modelState.value = ModelState.NotInstalled
                return
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(contextWindow)
                .setResultListener { partialResult, done ->
                    currentGenerationChannel?.trySend(partialResult ?: "")
                    if (done) {
                        currentGenerationChannel?.close()
                    }
                }
                .build()

            currentOptions = options
            lastSessionId = null
            llmInference = LlmInference.createFromOptions(context, options)

            _modelState.value = ModelState.Active(
                modelName = modelFileName,
                params = ModelParams(contextWindow = contextWindow, threadCount = threadCount)
            )
            _diagnostics.value = Diagnostics(0f, 1200) // Approx memory for 2B models
            Log.i("LlamaInferenceEngine", "MediaPipe LLM model loaded successfully.")

        } catch (e: Exception) {
            Log.e("LlamaInferenceEngine", "Failed to load model", e)
            _modelState.value = ModelState.Failed(com.example.backend.models.ErrorType.UNKNOWN, e.localizedMessage ?: "Unknown error")
        }
    }

    fun unloadModel() {
        llmInference?.close()
        llmInference = null
        _modelState.value = ModelState.NotInstalled
        _diagnostics.value = Diagnostics(0f, 0)
        System.gc()
        _modelState.value = com.example.backend.models.ModelState.Loading
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
    ): Flow<String> = flow {
        Log.i("LlamaInferenceEngine", "Generating for job $jobId, mode $mode")
        if (lastSessionId != null && lastSessionId != jobId) {
            Log.i("LlamaInferenceEngine", "Switching conversation session. Recreating LlmInference.")
            currentOptions?.let { 
                llmInference?.close()
                llmInference = LlmInference.createFromOptions(context, it)
            }
        }

        val isNewSession = (lastSessionId != jobId) || (llmInference == null)
        lastSessionId = jobId
        val effectiveHistory = if (isNewSession) history else emptyList<ChatMessage>()

        val builtPrompt = PromptManager.buildPrompt(manifest, effectiveHistory, newTask, mode, targetLanguage, isNewSession)

        if (llmInference == null) {
            emit("Error: No local model loaded. Please go to the Models hub and download a model to run inference natively on your device.")
            return@flow
        }

        inferenceMutex.withLock {
            val channel = Channel<String>(Channel.UNLIMITED)
            currentGenerationChannel = channel
            try {
                llmInference!!.generateResponseAsync(builtPrompt)
                for (chunk in channel) {
                    emit(chunk)
                }
            } catch (e: Exception) {
                Log.e("LlamaInferenceEngine", "Error in generation", e)
                emit("Error: Model crashed during generation. ${e.message}")
            } finally {
                currentGenerationChannel = null
            }
        }
    }.flowOn(Dispatchers.IO).onCompletion {
        _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
    }

    fun generate(prompt: String, config: GenerationConfig = GenerationConfig(), jobId: String = java.util.UUID.randomUUID().toString()): Flow<String> = flow {
        Log.i("LlamaInferenceEngine", "Generating custom prompt for job $jobId")
        
        if (lastSessionId != null && lastSessionId != jobId) {
            Log.i("LlamaInferenceEngine", "Switching conversation session. Recreating LlmInference.")
            currentOptions?.let { 
                llmInference?.close()
                llmInference = LlmInference.createFromOptions(context, it)
            }
        }
        lastSessionId = jobId
        
        if (llmInference == null) {
            emit("Error: No local model loaded. Please download a model.")
            return@flow
        }
        
        inferenceMutex.withLock {
            val channel = Channel<String>(Channel.UNLIMITED)
            currentGenerationChannel = channel
            try {
                llmInference!!.generateResponseAsync(prompt)
                for (chunk in channel) {
                    emit(chunk)
                }
            } catch (e: Exception) {
                Log.e("LlamaInferenceEngine", "Error in generation", e)
                emit("Error: ${e.message}")
            } finally {
                currentGenerationChannel = null
            }
        }
    }.flowOn(Dispatchers.IO).onCompletion {
        _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
    }

    fun stopGeneration() {
        Log.i("LlamaInferenceEngine", "Stopping generation not natively supported by basic MediaPipe sync call.")
        _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
    }
}
