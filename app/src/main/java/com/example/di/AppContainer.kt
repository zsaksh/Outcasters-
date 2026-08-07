package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.SrsDatabase
import com.example.data.ModelRepositoryImpl
import com.example.domain.ModelRepository
import com.example.inference.LlamaInferenceEngine
import com.example.backend.inference.ModelManager
import com.example.backend.hf.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

class AppContainer(private val context: Context) {
    val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "outcasters_db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
    }
    
    val srsDatabase by lazy {
        Room.databaseBuilder(context, SrsDatabase::class.java, "srs_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    
    val chatDao by lazy { database.chatDao() }
    val ocrDao by lazy { database.ocrDao() }
    val modelManifestDao by lazy { srsDatabase.modelManifestDao() }
    
    val modelRepository: ModelRepository by lazy {
        ModelRepositoryImpl(context)
    }

    val chatRepository: com.example.data.ChatRepository by lazy {
        com.example.data.ChatRepository(chatDao)
    }

    val inferenceLogger: com.example.backend.inference.InferenceLogger by lazy {
        com.example.backend.inference.InferenceLogger(database.inferenceMetadataDao())
    }

    val downloadManager: DownloadManager by lazy {
        DownloadManager(context, modelManifestDao)
    }

    // A fully compliant lazy loader tracking db state
    val inferenceEngine: LlamaInferenceEngine by lazy {
        val engine = LlamaInferenceEngine()
        CoroutineScope(Dispatchers.IO).launch {
            modelManifestDao.getAllModels()
                .map { models -> models.find { it.activeStatus }?.fileName }
                .distinctUntilChanged()
                .collectLatest { fileName ->
                    if (fileName != null) {
                        engine.loadModel(fileName)
                    } else {
                        engine.unloadModel()
                    }
                }
        }
        engine
    }
}
