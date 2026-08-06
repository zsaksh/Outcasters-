package com.example.backend.inference

import com.example.backend.models.ModelManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The core abstraction layer for local AI execution.
 * Built to support swapping llama.cpp, ExecuTorch, or ONNX runtime seamlessly.
 */
interface IInferenceEngine {
    
    /**
     * MVI strict state flow representing the current lifecycle of the active model.
     */
    val state: StateFlow<InferenceState>

    /**
     * Initialize the engine with a specific model.
     * Uses zero-copy mmap internally to avoid JVM memory blowup.
     * Suspends until the model is loaded into memory or throws a caught exception.
     */
    suspend fun initialize(manifest: ModelManifest): Result<Unit>

    /**
     * Generates a response stream for the given formatted prompt.
     * Streams UTF-8 tokens safely across the JNI boundary to prevent UI freezing.
     */
    fun generate(prompt: String): Flow<String>

    /**
     * Immediately halts any ongoing generation by signaling the native runtime.
     * Cooperative cancellation ensures no memory leaks.
     */
    fun stop()

    /**
     * Unloads the current model from memory and explicitly frees native pointers.
     * Must be called aggressively when backgrounding to avoid OOM.
     */
    fun unload()
}
