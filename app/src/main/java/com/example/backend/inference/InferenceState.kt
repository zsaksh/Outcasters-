package com.example.backend.inference

import com.example.backend.models.ModelManifest

/**
 * MVI State for the Inference Engine.
 * Represents the strict Unidirectional Data Flow states of the local AI execution.
 */
sealed class InferenceState {
    object Uninitialized : InferenceState()
    
    data class Loading(val manifest: ModelManifest, val progress: Float = 0f) : InferenceState()
    
    data class Ready(val manifest: ModelManifest) : InferenceState()
    
    data class Generating(
        val manifest: ModelManifest,
        val tokensGenerated: Int,
        val currentText: String,
        val tokensPerSecond: Float = 0f
    ) : InferenceState()
    
    data class Error(val throwable: Throwable, val isNativeCrash: Boolean = false) : InferenceState()
}
