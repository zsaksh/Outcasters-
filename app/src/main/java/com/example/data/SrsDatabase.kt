package com.example.data

import androidx.room.*
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelManifestDao
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "failed_concepts")
data class FailedConcept(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val wordOrConcept: String,
    val errorType: String,
    val userUsed: String,
    val correctAnswer: String,
    val halfLife: Float = 0.5f,
    val nextReviewTime: Long
)

@Entity(tableName = "interview_sessions")
data class InterviewSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val targetRole: String,
    val domain: String,
    val transcriptJson: String,
    val structureScore: Int,
    val technicalScore: Int,
    val communicationScore: Int,
    val impactScore: Int,
    val overallRecommendation: String,
    val feedbackJson: String
)

@Dao
interface FailedConceptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(concept: FailedConcept)

    @Query("SELECT * FROM failed_concepts WHERE nextReviewTime <= :currentTime ORDER BY nextReviewTime ASC")
    fun getDueConcepts(currentTime: Long): Flow<List<FailedConcept>>
    
    @Query("SELECT * FROM failed_concepts")
    fun getAllConcepts(): Flow<List<FailedConcept>>
}

@Dao
interface InterviewSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: InterviewSession)

    @Query("SELECT * FROM interview_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<InterviewSession>>
}

@Database(entities = [FailedConcept::class, InterviewSession::class, ModelManifest::class], version = 4, exportSchema = false)
abstract class SrsDatabase : RoomDatabase() {
    abstract fun failedConceptDao(): FailedConceptDao
    abstract fun interviewSessionDao(): InterviewSessionDao
    abstract fun modelManifestDao(): ModelManifestDao
}
