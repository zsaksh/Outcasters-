package com.example.backend.manager

import kotlinx.coroutines.flow.StateFlow

interface IThermalMemoryManager {
    val memoryState: StateFlow<MemoryPressureLevel>
    val thermalState: StateFlow<ThermalStateLevel>
    
    /**
     * Starts listening to OS-level broadcast receivers for memory/thermal pressure.
     */
    fun startMonitoring()
    fun stopMonitoring()
    
    /**
     * Should be called when the system reports low memory, allowing the engine to unload models.
     */
    fun onLowMemoryDetected()
}

enum class MemoryPressureLevel {
    NORMAL, MODERATE, CRITICAL
}

enum class ThermalStateLevel {
    NONE, LIGHT, MODERATE, SEVERE, CRITICAL
}
