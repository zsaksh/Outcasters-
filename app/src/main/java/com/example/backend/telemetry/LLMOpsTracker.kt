package com.example.backend.telemetry

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "telemetry_logs")
data class TelemetryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String, // e.g., "OOM_CRASH", "NEGATIVE_SIGNAL"
    val modelId: String,
    val timestamp: Long,
    val details: String
)

@Dao
interface TelemetryDao {
    @Insert
    suspend fun insertLog(log: TelemetryLog)
    
    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TelemetryLog>>
    
    @Query("SELECT COUNT(*) FROM telemetry_logs WHERE eventType = 'OOM_CRASH' AND modelId = :modelId")
    suspend fun getOomCount(modelId: String): Int
}

class LLMOpsTracker(private val dao: TelemetryDao) {
    suspend fun logFailure(modelId: String, reason: String) {
        dao.insertLog(TelemetryLog(
            eventType = "MODEL_FAILURE",
            modelId = modelId,
            timestamp = System.currentTimeMillis(),
            details = reason
        ))
    }
    
    suspend fun logOom(modelId: String) {
        dao.insertLog(TelemetryLog(
            eventType = "OOM_CRASH",
            modelId = modelId,
            timestamp = System.currentTimeMillis(),
            details = "Out of memory during initialization or inference"
        ))
    }

    suspend fun logNegativeSignal(modelId: String, reason: String) {
        dao.insertLog(TelemetryLog(
            eventType = "NEGATIVE_SIGNAL",
            modelId = modelId,
            timestamp = System.currentTimeMillis(),
            details = reason // e.g., "User stopped early" or "Regenerated"
        ))
    }
}
