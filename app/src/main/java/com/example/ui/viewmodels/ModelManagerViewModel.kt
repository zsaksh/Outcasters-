package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelManifestDao
import com.example.backend.models.initialModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ModelManagerViewModel(private val dao: ModelManifestDao) : ViewModel() {
    
    val allModels = dao.getAllModels()
    
    private val _activeModel = MutableStateFlow<ModelManifest?>(null)
    val activeModel: StateFlow<ModelManifest?> = _activeModel.asStateFlow()
    
    init {
        viewModelScope.launch {
            val currentModels = dao.getAllModels().first()
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
            // Ensure only fully downloaded and verified models can be selected
            if (model != null && model.downloadStatus == "downloaded") {
                dao.setActiveModelAtomic(modelId)
            }
        }
    }
    
    fun startDownload(modelId: String) {
        viewModelScope.launch {
            val model = dao.getModelById(modelId)
            if (model != null && model.downloadStatus == "not_installed") {
                dao.update(model.copy(downloadStatus = "downloading"))
                
                // Simulate download
                delay(2000)
                
                // Transition to verifying state
                dao.update(model.copy(downloadStatus = "verifying"))
                
                // Simulate hash verification
                delay(1500)
                
                // Complete download and mark as verified/downloaded
                dao.update(model.copy(downloadStatus = "downloaded", installStatus = "ready"))
            }
        }
    }

    fun deleteModel(context: android.content.Context, modelId: String) {
        viewModelScope.launch {
            val model = dao.getModelById(modelId)
            if (model != null) {
                // delete file
                val file = java.io.File(context.filesDir, "models/${model.fileName}")
                if (file.exists()) {
                    file.delete()
                }
                
                // if it was a pre-packaged model, just update status. If it was downloaded, maybe delete from db
                if (model.modelId.length > 20) {
                    // Random UUID means it was from HuggingFace
                    dao.delete(model)
                } else {
                    dao.update(model.copy(downloadStatus = "not_installed", activeStatus = false))
                }
            }
        }
    }
}

class ModelManagerViewModelFactory(private val dao: ModelManifestDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModelManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModelManagerViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
