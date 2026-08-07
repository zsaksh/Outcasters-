package com.example.backend.download

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class GgufFileMetadata(
    val fileName: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val quantization: String
)

class ModelRepositoryResolver(private val client: HttpClient = HttpClient(Android)) {
    
    suspend fun resolveCompatibleGgufFiles(repoId: String): List<GgufFileMetadata> = withContext(Dispatchers.IO) {
        Log.d("ModelRepositoryResolver", "Resolving GGUFs for \$repoId")
        
        val apiUrl = "https://huggingface.co/api/models/\$repoId/tree/main"
        val variants = mutableListOf<GgufFileMetadata>()
        
        try {
            val response = client.get(apiUrl)
            val bodyStr = response.bodyAsText()
            val jsonArray = JSONArray(bodyStr)
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val type = item.optString("type")
                val path = item.optString("path")
                val size = item.optLong("size", 0L)
                
                if (type == "file" && path.endsWith(".tflite", ignoreCase = true)) {
                    val quant = extractQuantization(path)
                    val downloadUrl = "https://huggingface.co/\$repoId/resolve/main/\$path"
                    
                    variants.add(GgufFileMetadata(
                        fileName = path,
                        sizeBytes = size,
                        downloadUrl = downloadUrl,
                        quantization = quant
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("ModelRepositoryResolver", "Error resolving repo: \${e.message}")
        }
        
        return@withContext variants
    }
    
    private fun extractQuantization(fileName: String): String {
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
