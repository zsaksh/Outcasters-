package com.example.backend.inference

import com.example.backend.models.ModelManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

interface InferenceEngine {
    suspend fun initialize(model: ModelManifest): Boolean
    fun generate(prompt: String): Flow<String>
    suspend fun stop()
    suspend fun unload()
    fun isReady(): Boolean
}

class LlamaCppEngine : InferenceEngine {
    private var isLoaded = false
    private var currentModelId: String? = null

    override suspend fun initialize(model: ModelManifest): Boolean {
        // Mocking C++ load
        delay(1000)
        isLoaded = true
        currentModelId = model.modelId
        return true
    }

    override fun generate(prompt: String): Flow<String> = flow {
        if (!isLoaded) throw IllegalStateException("Engine not loaded")
        val words = "This is a simulated streaming response from the active local inference engine for model \${currentModelId}.".split(" ")
        for (word in words) {
            delay(100)
            emit("$word ")
        }
    }

    override suspend fun stop() {
        // Safe stop logic for ongoing generation
    }

    override suspend fun unload() {
        // Safe memory unloading logic
        isLoaded = false
        currentModelId = null
    }

    override fun isReady(): Boolean = isLoaded
}
