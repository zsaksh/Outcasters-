package com.example.backend.models

val initialModels = listOf(
    ModelManifest(
        modelId = "gemma-2b-cpu",
        displayName = "Gemma 2B",
        sourceUrl = "https://huggingface.co/xianbao/mediapipe-gemma-2b-it/resolve/main/gemma_cpu.tflite",
        fileName = "gemma_cpu.tflite",
        quantization = "INT4", format = "TFLITE", chatTemplate = "gemma",
        fileSizeBytes = 1800L * 1024 * 1024L,
        estimatedRamMB = 2200,
        compatibilityFlag = "Standard",
        installStatus = "not_installed"
    )
)
