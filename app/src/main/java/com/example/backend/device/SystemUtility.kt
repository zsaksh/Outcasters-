package com.example.backend.device

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.backend.models.DeviceTier

data class SystemSpecs(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val totalStorageMb: Long,
    val availableStorageMb: Long,
    val cpuCores: Int
)

class SystemUtility(private val context: Context) {
    
    fun getSystemSpecs(): SystemSpecs {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableStorageBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalStorageBytes = stat.blockCountLong * stat.blockSizeLong
        
        return SystemSpecs(
            totalRamMb = memoryInfo.totalMem / (1024 * 1024),
            availableRamMb = memoryInfo.availMem / (1024 * 1024),
            totalStorageMb = totalStorageBytes / (1024 * 1024),
            availableStorageMb = availableStorageBytes / (1024 * 1024),
            cpuCores = Runtime.getRuntime().availableProcessors()
        )
    }

    /**
     * Suggest an appropriate model tier based on system capabilities.
     * Scales gracefully avoiding cloud-dependency.
     */
    fun suggestModelTier(): DeviceTier {
        val specs = getSystemSpecs()
        
        return when {
            specs.totalRamMb >= 12000 -> DeviceTier.ULTRA
            specs.totalRamMb >= 8000 -> DeviceTier.FLAGSHIP
            specs.totalRamMb >= 6000 -> DeviceTier.ADVANCED
            specs.totalRamMb >= 4000 -> DeviceTier.STANDARD
            else -> DeviceTier.LITE
        }
    }

    fun getMaxAllocatedRamMb(): Long {
        val specs = getSystemSpecs()
        val osMarginMb = 1800L
        val appOverheadMb = 400L
        val maxAllocated = specs.availableRamMb - osMarginMb - appOverheadMb
        return Math.max(0L, maxAllocated)
    }

    fun canSafelyLoadModel(estimatedModelMemoryMb: Long, contextLength: Int, layers: Int = 32): Boolean {
        val kvCacheOverheadMb = (contextLength * layers * 1.5).toLong() / 1024
        val requiredRamMb = estimatedModelMemoryMb + kvCacheOverheadMb
        return requiredRamMb <= getMaxAllocatedRamMb()
    }
}
