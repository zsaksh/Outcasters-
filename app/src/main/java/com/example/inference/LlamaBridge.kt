package com.example.inference

interface TokenCallback {
    fun onTokenGenerated(token: String)
    fun onError(error: String)
    fun onComplete()
}

class LlamaBridge {
    external fun clearKvCache(): Boolean
    external fun isModelReady(): Boolean
    private external fun nativeGenerateStream(prompt: String, callback: TokenCallback)

    fun generateStreamSafely(prompt: String): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.channelFlow {
        // 1. Pre-execution Check
        if (!isModelReady()) {
            throw IllegalStateException("Cannot run query: Native model is not loaded in memory.")
        }

        if (prompt.isBlank()) {
            close()
            return@channelFlow
        }

        // 2. JNI Native Callback
        val callback = object : TokenCallback {
            override fun onTokenGenerated(token: String) {
                trySend(token)
            }

            override fun onError(error: String) {
                close(RuntimeException("Native Inference Error: \$error"))
            }

            override fun onComplete() {
                close()
            }
        }

        nativeGenerateStream(prompt, callback)
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
