package com.example.backend.models

val initialModels = listOf(
    ModelManifest(
        modelId = "qwen-2.5-0.5b-instruct",
        displayName = "Qwen 2.5 0.5B Instruct",
        repoId = "Qwen/Qwen2.5-0.5B-Instruct-GGUF",
        sourceUrl = "",
        fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        quantization = "Q4_K_M",
        fileSizeBytes = 450 * 1024 * 1024L, // ~450MB
        deviceRecommendationTier = "Lite",
        downloadStatus = "not_installed",
        installStatus = "pending",
        activeStatus = false
    ),
    ModelManifest(
        modelId = "llama-3-8b-instruct",
        displayName = "Llama 3 8B Instruct",
        repoId = "meta-llama/Meta-Llama-3-8B-Instruct-GGUF",
        sourceUrl = "",
        fileName = "llama-3-8b-instruct.Q4_K_M.gguf",
        quantization = "Q4_K_M",
        fileSizeBytes = 4500L * 1024 * 1024L, // ~4.5GB
        deviceRecommendationTier = "Advanced",
        downloadStatus = "not_installed",
        installStatus = "pending",
        activeStatus = false
    ),
    ModelManifest(
        modelId = "bonsai-1b",
        displayName = "Bonsai 1B (Mock)",
        repoId = "bonsai/bonsai-1b-gguf",
        sourceUrl = "",
        fileName = "bonsai-1b-q4.gguf",
        quantization = "Q4",
        fileSizeBytes = 800L * 1024 * 1024L, // ~800MB
        deviceRecommendationTier = "Standard",
        downloadStatus = "not_installed",
        installStatus = "pending",
        activeStatus = false
    ),
    ModelManifest(
        modelId = "outcasters-3b",
        displayName = "Outcasters 3B",
        repoId = "outcasters/outcasters-3b-gguf",
        sourceUrl = "",
        fileName = "outcasters-3b-q5.gguf",
        quantization = "Q5",
        fileSizeBytes = 2000L * 1024 * 1024L, // ~2GB
        deviceRecommendationTier = "Standard",
        downloadStatus = "not_installed",
        installStatus = "pending",
        activeStatus = false
    )
)
