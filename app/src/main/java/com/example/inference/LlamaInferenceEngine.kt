package com.example.inference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.example.backend.models.ModelState
import com.example.backend.models.ModelParams

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

    fun loadModel(modelPath: String) {
        unloadModel()
        System.gc() // Safely dispose of current model pointers in simulated JNI
        
        // Implementation for loading GGUF via llama.cpp JNI
        _modelState.value = ModelState.Active(
            modelName = modelPath.substringAfterLast("/"),
            params = ModelParams(contextWindow = contextWindow, threadCount = threadCount)
        )
        _diagnostics.value = Diagnostics(0f, Random.nextInt(800, 2000))
    }
    
    fun unloadModel() {
        _modelState.value = ModelState.NotInstalled
        _diagnostics.value = Diagnostics(0f, 0)
        System.gc() // Safely dispose of current model pointers in simulated JNI
    }
    
    fun generate(prompt: String): Flow<String> = flow {
        if (_modelState.value !is ModelState.Active) throw IllegalStateException("Model not loaded")
        
        // Simulating processing state by keeping it Active but updating diagnostics
        val words = "This is a local inference response from the model based on the extracted context. (Threads: $threadCount, Ctx: $contextWindow)".split(" ")
        for (word in words) {
            emit("$word ")
            _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = Random.nextFloat() * 15 + 10)
            delay(100)
        }
        _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
    }
    
    fun stopGeneration() {
        if (_modelState.value is ModelState.Active) {
            _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
        }
    }
}
