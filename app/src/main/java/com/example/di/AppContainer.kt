package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.SrsDatabase
import com.example.data.ModelRepositoryImpl
import com.example.domain.ModelRepository
import com.example.inference.LlamaInferenceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

class AppContainer(private val context: Context) {
    val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "outcasters_db")
            .fallbackToDestructiveMigration()
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
