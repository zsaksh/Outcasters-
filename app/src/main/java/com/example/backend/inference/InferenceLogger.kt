package com.example.backend.inference

import com.example.data.InferenceMetadataDao
import com.example.data.InferenceMetadataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InferenceLogger(private val metadataDao: InferenceMetadataDao) {
    fun logMetrics(
        sessionId: Long,
        latencyMs: Long,
        tokenCount: Int,
        memoryUsageMb: Int,
        model: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val metadata = InferenceMetadataEntity(
                sessionId = sessionId,
                latencyMs = latencyMs,
                tokenCount = tokenCount,
                memoryUsageMb = memoryUsageMb,
                model = model,
                timestamp = System.currentTimeMillis()
            )
            metadataDao.insertMetadata(metadata)
        }
    }
}
