package com.example.inference

import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference

fun dumpLlmInferenceMethods() {
    try {
        val methods = LlmInference::class.java.methods
        for (m in methods) {
            Log.d("LlmReflection", "Method: " + m.name)
        }
    } catch (e: Exception) {
        Log.e("LlmReflection", "Error", e)
    }
}
