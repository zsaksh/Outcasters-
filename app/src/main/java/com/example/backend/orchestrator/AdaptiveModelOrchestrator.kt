package com.example.backend.orchestrator

import com.example.backend.models.ModelManifest

/**
 * Routes tasks to specialized local models to maximize responsiveness and minimize battery drain.
 * Each task is executed on the smallest model capable of solving it.
 */
interface AdaptiveModelOrchestrator {
    
    suspend fun getOptimalModelForTask(taskType: TaskType): ModelManifest?
    
    suspend fun routeTask(taskType: TaskType, payload: String): OrchestrationResult
}

enum class TaskType {
    OCR_EXTRACTION,
    EMBEDDING_GENERATION,
    TRANSLATION,
    GRAMMAR_CORRECTION,
    GENERAL_QA,
    DEEP_REASONING,
    VISION_ANALYSIS
}

data class OrchestrationResult(
    val executedModelId: String,
    val result: String,
    val latencyMs: Long
)
