package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelManifestDao
import com.example.backend.models.initialModels
import com.example.backend.hf.DownloadManager
import com.example.backend.inference.ModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class ModelManagerViewModel(
    private val dao: ModelManifestDao,
    private val downloadManager: DownloadManager
) : ViewModel() {

    val allModels = dao.getAllModels()

    private val _activeModel = MutableStateFlow<ModelManifest?>(null)
    val activeModel: StateFlow<ModelManifest?> = _activeModel.asStateFlow()

    init {
        viewModelScope.launch {
            val currentModels = dao.getAllModels().first()
            currentModels.forEach { if (it.chatTemplate.isEmpty()) { dao.update(it.copy(chatTemplate = "gemma")) } }
            if (currentModels.isEmpty()) {
                initialModels.forEach { dao.insert(it) }
            }

            dao.getAllModels().collectLatest { models ->
                _activeModel.value = models.find { it.activeStatus }
            }
        }
    }

    fun activateModel(modelId: String) {
        viewModelScope.launch {
            val model = dao.getModelById(modelId)
            if (model != null && model.installStatus == "ready") {
                dao.setActiveModelAtomic(modelId)
            }
        }
    }

    fun startDownload(modelId: String) {
        viewModelScope.launch {
            downloadManager.startDownload(modelId)
        }
    }

    fun pauseDownload(modelId: String) {
        viewModelScope.launch {
            downloadManager.pauseDownload(modelId)
        }
    }

    fun retryDownload(modelId: String) {
        viewModelScope.launch {
            downloadManager.retryDownload(modelId)
        }
    }

    fun deleteModel(context: Context, modelId: String) {
        viewModelScope.launch {
            val model = dao.getModelById(modelId)
            if (model != null) {
                // pause if downloading
                if (model.installStatus == "downloading") {
                    downloadManager.pauseDownload(modelId)
                }

                // delete file
                val file = File(context.filesDir, "models/${model.fileName}")
                if (file.exists()) {
                    file.delete()
                }

                // reset status
                dao.update(model.copy(
                    installStatus = "not_installed", 
                    activeStatus = false,
                    downloadProgress = 0,
                    fileSizeBytes = 0L
                ))
            }
        }
    }
}

class ModelManagerViewModelFactory(
    private val dao: ModelManifestDao,
    private val downloadManager: DownloadManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModelManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModelManagerViewModel(dao, downloadManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
