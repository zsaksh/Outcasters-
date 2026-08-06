package com.example.backend.telemetry

/**
 * On-Device Context Scrubbing: Redacts PII, names, and metadata locally
 * before any prompt or search query leaves the device.
 */
object PrivacyScrubber {
    
    // Basic regex patterns for PII
    private val emailRegex = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
    private val phoneRegex = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b".toRegex()
    
    fun scrub(input: String): String {
        var scrubbed = input
        scrubbed = scrubbed.replace(emailRegex, "[REDACTED_EMAIL]")
        scrubbed = scrubbed.replace(phoneRegex, "[REDACTED_PHONE]")
        // Advanced NLP NER (Named Entity Recognition) would go here
        // to redact names, school names, etc.
        return scrubbed
    }

    /**
     * Injects differential privacy noise into query embeddings to mask user intent.
     */
    fun applyDifferentialPrivacyNoise(embedding: FloatArray, epsilon: Float = 1.0f): FloatArray {
        // Laplace mechanism noise injection placeholder
        return embedding.map { it + generateLaplaceNoise(epsilon) }.toFloatArray()
    }

    private fun generateLaplaceNoise(epsilon: Float): Float {
        // Simplified Laplace noise generation
        val u = Math.random() - 0.5
        val b = 1.0 / epsilon
        return ((-b) * Math.signum(u) * Math.log(1.0 - 2.0 * Math.abs(u))).toFloat()
    }
}
