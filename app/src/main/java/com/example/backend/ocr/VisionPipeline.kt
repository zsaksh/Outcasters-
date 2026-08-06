package com.example.backend.ocr

import android.graphics.Bitmap

/**
 * Intelligent vision pipeline supporting text, formulas, and multimodal projections (mmproj).
 */
interface VisionPipeline {
    suspend fun preprocessImage(image: Bitmap): Bitmap
    suspend fun extractText(image: Bitmap): String
    suspend fun extractMathFormula(image: Bitmap): String
    suspend fun generateImageEmbedding(image: Bitmap): FloatArray // For LLaVA mmproj routing
}
