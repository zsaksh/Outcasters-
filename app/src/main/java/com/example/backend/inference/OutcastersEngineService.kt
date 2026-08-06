package com.example.backend.inference

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.util.Log
import com.example.inference.LlamaInferenceEngine

class OutcastersEngineService : Service() {

    private val binder = LocalBinder()
    private val engine = LlamaInferenceEngine() // We'll move the engine here or use Native bindings
    
    inner class LocalBinder : Binder() {
        fun getService(): OutcastersEngineService = this@OutcastersEngineService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    fun loadModel(path: String, contextLength: Int, threads: Int) {
        Log.d("OutcastersEngine", "Loading model via JNI: $path with ctx=$contextLength threads=$threads")
        engine.contextWindow = contextLength
        engine.threadCount = threads
        engine.loadModel(path)
    }
    
    fun unloadModel() {
        Log.d("OutcastersEngine", "Unloading model via JNI")
        engine.unloadModel()
    }
    
    fun generate(prompt: String) = engine.generate(prompt)
    
    fun stopGeneration() {
        engine.stopGeneration()
    }
    
    fun getDiagnostics() = engine.diagnostics
    fun getModelState() = engine.modelState
}
