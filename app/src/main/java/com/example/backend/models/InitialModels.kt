package com.example.backend.models

val initialModels = listOf(
    ModelManifest(
        modelId = "smollm2-360m",
        displayName = "SmolLM2 360M",
        sourceUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf",
        fileName = "smollm2-360m-instruct-q8_0.gguf",
        quantization = "Q8_0",
        fileSizeBytes = 380L * 1024 * 1024L,
        estimatedRamMB = 500,
        compatibilityFlag = "Lite",
        installStatus = "not_installed"
    ),
    ModelManifest(
        modelId = "qwen-2.5-0.5b",
        displayName = "Qwen 2.5 0.5B",
        sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q8_0.gguf",
        fileName = "qwen2.5-0.5b-instruct-q8_0.gguf",
        quantization = "Q8_0",
        fileSizeBytes = 550L * 1024 * 1024L,
        estimatedRamMB = 700,
        compatibilityFlag = "Lite",
        installStatus = "not_installed"
    ),
    ModelManifest(
        modelId = "llama-3.2-1b",
        displayName = "Llama 3.2 1B",
        sourceUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
        fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
        quantization = "Q4_K_M",
        fileSizeBytes = 850L * 1024 * 1024L,
        estimatedRamMB = 1200,
        compatibilityFlag = "Standard",
        installStatus = "not_installed"
    ),
    ModelManifest(
        modelId = "phi-3.5-mini",
        displayName = "Phi-3.5 mini",
        sourceUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
        fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
        quantization = "Q4_K_M",
        fileSizeBytes = 2400L * 1024 * 1024L,
        estimatedRamMB = 3000,
        compatibilityFlag = "Advanced",
        installStatus = "not_installed"
    )
)
