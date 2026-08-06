package com.example.backend.models

interface IModelStore {
    suspend fun saveManifest(manifest: ModelManifest)
    suspend fun getManifest(modelId: String): ModelManifest?
    suspend fun getAllManifests(): List<ModelManifest>
    suspend fun deleteManifest(modelId: String)
    suspend fun updateModelStatus(modelId: String, status: String)
}
