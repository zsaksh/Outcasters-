package com.example.backend.interview

import android.util.Log

class SileroVadClient {
    
    // We would normally load the C++ library here
    init {
        try {
            System.loadLibrary("silero_vad_jni")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("SileroVadClient", "Native library not found, falling back to mock VAD")
        }
    }

    /**
     * Initializes the Silero VAD model via JNI.
     */
    external fun initVadModel(modelPath: String): Boolean

    /**
     * Processes an audio frame (PCM 16-bit) and returns a speech probability.
     */
    external fun processFrame(audioData: ShortArray): Float

    /**
     * Closes the VAD model and releases resources.
     */
    external fun closeVadModel()

    // --- Mock Implementation for environment without NDK ---
    
    private var isMockInitialized = false

    fun initVadModelMock(modelPath: String): Boolean {
        isMockInitialized = true
        Log.d("SileroVadClient", "Mock VAD Initialized with path: \$modelPath")
        return true
    }

    fun processFrameMock(audioData: ShortArray): Float {
        if (!isMockInitialized) return 0f
        
        // Simple heuristic for mock: calculate energy of the frame
        var sum = 0L
        for (sample in audioData) {
            sum += sample * sample
        }
        val rms = Math.sqrt(sum.toDouble() / audioData.size)
        
        // Return a mock probability based on energy
        return if (rms > 500.0) 0.9f else 0.1f
    }

    fun closeVadModelMock() {
        isMockInitialized = false
        Log.d("SileroVadClient", "Mock VAD Closed")
    }
}
