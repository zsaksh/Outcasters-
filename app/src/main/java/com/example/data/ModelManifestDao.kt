package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelManifestDao {
    @Query("SELECT * FROM model_manifests")
    fun getAllManifests(): Flow<List<ModelManifestEntity>>

    @Query("SELECT * FROM model_manifests WHERE installStatus = 'installed'")
    fun getInstalledManifests(): Flow<List<ModelManifestEntity>>

    @Query("SELECT * FROM model_manifests WHERE active = 1 LIMIT 1")
    suspend fun getActiveManifest(): ModelManifestEntity?

    @Query("SELECT * FROM model_manifests WHERE modelId = :modelId")
    suspend fun getManifestById(modelId: String): ModelManifestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManifest(manifest: ModelManifestEntity)

    @Query("UPDATE model_manifests SET installStatus = :status WHERE modelId = :modelId")
    suspend fun updateStatus(modelId: String, status: String)

    @Query("UPDATE model_manifests SET active = 0")
    suspend fun deactivateAll()

    @Query("UPDATE model_manifests SET active = 1 WHERE modelId = :modelId")
    suspend fun setActive(modelId: String)

    @Query("DELETE FROM model_manifests WHERE modelId = :modelId")
    suspend fun deleteManifest(modelId: String)
}
