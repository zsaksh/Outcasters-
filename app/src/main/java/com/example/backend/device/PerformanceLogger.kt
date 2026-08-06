package com.example.backend.device

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PerformanceLogger {
    private val stats = mutableListOf<String>()

    fun logStat(tps: Float, memoryPercent: Float) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val statEntry = "{\"timestamp\": \"$timestamp\", \"tps\": $tps, \"memory_percent\": $memoryPercent}"
        stats.add(statEntry)
    }

    fun exportToJson(context: Context) {
        if (stats.isEmpty()) return
        
        try {
            val file = File(context.getExternalFilesDir(null), "performance_log_${System.currentTimeMillis()}.json")
            val jsonContent = "[\n${stats.joinToString(",\n")}\n]"
            file.writeText(jsonContent)
            Log.d("PerformanceLogger", "Exported performance log to ${file.absolutePath}")
            stats.clear()
        } catch (e: Exception) {
            Log.e("PerformanceLogger", "Failed to export logs", e)
        }
    }
}
