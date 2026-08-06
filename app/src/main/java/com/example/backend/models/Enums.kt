package com.example.backend.models

enum class DeviceTier {
    LITE, STANDARD, ADVANCED, FLAGSHIP, ULTRA
}

enum class ModelInstallStatus {
    NOT_INSTALLED, DOWNLOADING, INSTALLED, CORRUPTED, PAUSED, FAILED
}
