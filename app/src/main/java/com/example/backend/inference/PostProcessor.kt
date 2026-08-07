package com.example.backend.inference

import android.util.Log

class PostProcessor {
    fun cleanResponse(response: String): String {
        return response
            .replace("<|im_end|>", "")
            .replace("<|end|>", "")
            .replace("<|eot_id|>", "")
            .replace("</s>", "")
            .trim()
    }
    
    // Phase 9 & 10: Output Validation and Quality Filter
    fun validateQuality(query: String, response: String, mode: String): Boolean {
        val qLower = query.lowercase()
        val rLower = response.lowercase()
        
        Log.i("PostProcessor", "Validating output for mode: $mode")
        
        // Ensure response is not empty
        if (rLower.isBlank()) return false
        
        // Phase 9: Reject completely unrelated content (simple keyword anti-matching)
        if (qLower.contains("calculus") && (rLower.contains("photosynthesis") || rLower.contains("cooking") || rLower.contains("python"))) {
            Log.w("PostProcessor", "Quality Filter: Rejected due to domain mismatch")
            return false
        }
        if (qLower.contains("photosynthesis") && rLower.contains("calculus")) {
            Log.w("PostProcessor", "Quality Filter: Rejected due to domain mismatch")
            return false
        }
        if (qLower.contains("translate") && rLower.contains("photosynthesis")) {
            return false
        }
        
        // If mode is strictly language, ensure we aren't outputting math formulas
        if (mode in listOf("translate", "grammar", "vocabulary") && rLower.contains("integral of")) {
            Log.w("PostProcessor", "Quality Filter: Rejected math formula in language mode")
            return false
        }
        
        return true
    }
}
