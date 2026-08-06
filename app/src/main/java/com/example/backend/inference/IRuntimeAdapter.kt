package com.example.backend.inference

import com.example.backend.models.ModelManifest
import kotlinx.coroutines.flow.Flow

/**
 * Adapter interface allowing modular inference backends (llama.cpp, ExecuTorch, ONNX, MLC).
 */
interface IRuntimeAdapter {
    val engineName: String
    val isSupportedOnDevice: Boolean

    suspend fun loadModel(manifest: ModelManifest): Result<Unit>
    fun generateStream(prompt: String): Flow<String>
    fun stopGeneration()
    fun unloadModel()
}
