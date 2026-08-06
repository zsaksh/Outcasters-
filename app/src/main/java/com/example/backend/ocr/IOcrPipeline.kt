package com.example.backend.ocr

import android.graphics.Bitmap

interface IOcrPipeline {
    /**
     * Vision processing pipeline.
     */
    suspend fun extractText(image: Bitmap): Result<OcrResult>
}

data class OcrResult(
    val text: String,
    val confidence: Float,
    val blocks: List<String>
)
