package com.example.backend.download

import android.content.Context
import android.util.Log
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class GgufVariant(
    val fileName: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val quantization: String
)

class HuggingFaceRepository(private val context: Context, private val client: OkHttpClient = OkHttpClient()) {
    
    // Resolves a Hugging Face model repo (e.g., "google/gemma-3-1b-pt-qat-q4_0-gguf")
    // and finds all GGUF files and their quantizations.
    fun resolveGgufVariants(repoId: String): List<GgufVariant> {
        Log.d("HFRepo", "Resolving GGUFs for \$repoId")
        
        // Use Hugging Face Hub API to get repo tree
        // Format: https://huggingface.co/api/models/{repo_id}/tree/main
        val apiUrl = "https://huggingface.co/api/models/\$repoId/tree/main"
        
        val request = Request.Builder().url(apiUrl).build()
        val variants = mutableListOf<GgufVariant>()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: return emptyList()
                    val jsonArray = JSONArray(bodyStr)
                    
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val type = item.optString("type")
                        val path = item.optString("path")
                        val size = item.optLong("size", 0L)
                        
                        if (type == "file" && path.endsWith(".gguf", ignoreCase = true)) {
                            // Extract quantization from filename (heuristic)
                            val quant = extractQuantization(path)
                            val downloadUrl = "https://huggingface.co/\$repoId/resolve/main/\$path"
                            
                            variants.add(GgufVariant(
                                fileName = path,
                                sizeBytes = size,
                                downloadUrl = downloadUrl,
                                quantization = quant
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HFRepo", "Error resolving repo: \${e.message}")
        }
        
        return variants
    }
    
    private fun extractQuantization(fileName: String): String {
        // Simple regex or heuristic to find "Q4_K_M", "Q8_0", etc.
        val parts = fileName.split(Regex("[.-_]"))
        for (part in parts) {
            val upper = part.uppercase()
            if (upper.matches(Regex("Q\\d.*|IQ\\d.*"))) {
                return upper
            }
        }
        return "Unknown"
    }
}
