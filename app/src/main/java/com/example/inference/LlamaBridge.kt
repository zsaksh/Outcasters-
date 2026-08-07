package com.example.inference

import android.util.Log

interface TokenCallback {
    fun onTokenGenerated(token: String)
    fun onError(error: String)
    fun onComplete()
}

class LlamaBridge : InferenceState {
    override external fun clearKvCache(): Boolean
    override external fun isModelReady(): Boolean
    external fun loadModel(modelPath: String): Boolean
    private external fun nativeGenerateStream(prompt: String, temperature: Float, maxTokens: Int, callback: TokenCallback)
    
    external fun cancelGeneration()
    override external fun resetSampler()
    override external fun clearDecoderState()
    override external fun createFreshInferenceState()
    
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
            try {
                System.loadLibrary("native-lib")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("LlamaBridge", "Failed to load native library: ${e.message}")
            }
        }
    }
}
