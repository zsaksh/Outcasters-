package com.example.inference

class LlamaBridge {
    external fun clearKvCache(): Boolean

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
