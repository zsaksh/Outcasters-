package com.example.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HuggingFaceDownloader {
    
    fun downloadModel(url: String, destinationPath: String): Flow<Int> = flow {
        // Mock download progress
        for (i in 1..100) {
            emit(i)
            kotlinx.coroutines.delay(50)
        }
    }
}
