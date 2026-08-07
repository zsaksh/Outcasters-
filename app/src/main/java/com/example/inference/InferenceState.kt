package com.example.inference

interface InferenceState {
    fun clearKvCache(): Boolean
    fun resetSampler()
    fun clearDecoderState()
    fun createFreshInferenceState()
    fun isModelReady(): Boolean
}
