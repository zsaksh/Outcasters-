package com.example.backend.inference

import android.util.Log
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelManifestDao
import com.example.backend.models.ModelState
import com.example.backend.models.ModelParams
import com.example.backend.models.ErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ModelManager(
    private val modelDao: ModelManifestDao,
    private val inferenceEngine: InferenceEngine
) {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotInstalled)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private val switchMutex = Mutex()

    suspend fun activateModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        switchMutex.withLock {
            val model = modelDao.getModelById(modelId)
            if (model == null) {
                Log.e("ModelManager", "Model $modelId not found in manifest")
                return@withLock false
            }

            if (model.activeStatus && inferenceEngine.isReady()) {
                Log.d("ModelManager", "Model $modelId is already active and loaded.")
                return@withLock true
            }

            _modelState.value = ModelState.Loading

            val previousActive = modelDao.getActiveModel()
            
            // Unload current model safely
            inferenceEngine.unload()

            // Verify
            if (model.installStatus != "ready") {
                _modelState.value = ModelState.Verifying
                val isVerified = verifyModel(model)
                if (!isVerified) {
                    _modelState.value = ModelState.Failed(ErrorType.CORRUPTION, "Verification failed")
                    return@withLock false
                }
            }

            // Load new model
            val loadSuccess = inferenceEngine.initialize(model)
            if (loadSuccess) {
                modelDao.setActiveModelAtomic(modelId)
                _modelState.value = ModelState.Active(model.displayName, ModelParams())
                return@withLock true
            } else {
                // Atomic failure recovery: restore previous
                Log.e("ModelManager", "Failed to load ${model.displayName}. Restoring previous.")
                modelDao.update(model.copy(installStatus = "corrupted", errorState = "Load failed"))
                
                if (previousActive != null && previousActive.modelId != modelId) {
                    inferenceEngine.initialize(previousActive)
                    modelDao.setActiveModelAtomic(previousActive.modelId)
                    _modelState.value = ModelState.Active(previousActive.displayName, ModelParams())
                } else {
                    _modelState.value = ModelState.Failed(ErrorType.UNKNOWN, "Failed to load model and no fallback available.")
                }
                return@withLock false
            }
        }
    }

    private fun verifyModel(model: ModelManifest): Boolean {
        // Mock verification
        return true
    }
}
