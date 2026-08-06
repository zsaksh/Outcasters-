package com.example.backend.inference

/**
 * Maps all native C++ errors and engine failures to readable Kotlin sealed classes.
 */
sealed class InferenceError : Exception() {
    data class CorruptedModelFile(override val message: String = "Model file is corrupted or incomplete.") : InferenceError()
    data class HardwareIncompatibility(override val message: String = "Device hardware does not support this model.") : InferenceError()
    data class GenerationTimeout(override val message: String = "Model generation timed out.") : InferenceError()
    data class OutOfMemory(override val message: String = "Device ran out of memory during inference.") : InferenceError()
    data class UnknownNativeError(val errorCode: Int, override val message: String) : InferenceError()
}
