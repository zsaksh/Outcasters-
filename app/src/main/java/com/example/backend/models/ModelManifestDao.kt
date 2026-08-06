package com.example.backend.models

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelManifestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: ModelManifest)

    @Update
    suspend fun update(model: ModelManifest)

    @Delete
    suspend fun delete(model: ModelManifest)

    @Query("SELECT * FROM models")
    fun getAllModels(): Flow<List<ModelManifest>>

    @Query("DELETE FROM models")
    suspend fun deleteAllModels()

    @Query("SELECT * FROM models WHERE modelId = :modelId LIMIT 1")
    suspend fun getModelById(modelId: String): ModelManifest?

    @Query("SELECT * FROM models WHERE activeStatus = 1 LIMIT 1")
    suspend fun getActiveModel(): ModelManifest?

    @Query("UPDATE models SET activeStatus = 0")
    suspend fun deactivateAll()

    @Transaction
    suspend fun setActiveModelAtomic(modelId: String) {
        deactivateAll()
        getModelById(modelId)?.let {
            update(it.copy(activeStatus = true))
        }
    }
}
