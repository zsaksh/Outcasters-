package com.example.backend.models

import kotlinx.coroutines.flow.StateFlow

interface IModelRegistry {
    val availableModels: StateFlow<List<ModelManifest>>
    val activeModel: StateFlow<ModelManifest?>
    
    suspend fun registerModel(manifest: ModelManifest)
    suspend fun setActiveModel(modelId: String): Boolean
    suspend fun getRecommendedModel(deviceRamMb: Int): ModelManifest?
}
