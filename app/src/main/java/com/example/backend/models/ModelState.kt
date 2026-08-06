package com.example.backend.models

enum class ErrorType {
    NETWORK,
    DISK_SPACE,
    CORRUPTION,
    OUT_OF_MEMORY,
    UNKNOWN
}

data class ModelParams(
    val contextWindow: Int = 2048,
    val threadCount: Int = 4,
    val useMlock: Boolean = false,
    val useGpu: Boolean = false
)

sealed class ModelState {
    object NotInstalled : ModelState()
    data class Downloading(val progress: Float) : ModelState()
    object Verifying : ModelState()
    object Ready : ModelState()
    object Loading : ModelState()
    data class Active(val modelName: String, val params: ModelParams) : ModelState()
    data class Failed(val errorType: ErrorType, val message: String) : ModelState()
    object Corrupted : ModelState()
    object Unsupported : ModelState()
}
