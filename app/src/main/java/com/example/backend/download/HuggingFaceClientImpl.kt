package com.example.backend.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class HuggingFaceClientImpl(private val client: OkHttpClient) : IHuggingFaceClient {
    
    override suspend fun resolveRepository(repoUrl: String): List<HfModelFile> = withContext(Dispatchers.IO) {
        val repoId = extractRepoId(repoUrl) ?: return@withContext emptyList()
        val apiUrl = "https://huggingface.co/api/models/$repoId/tree/main"
        
        val request = Request.Builder().url(apiUrl).build()
        val files = mutableListOf<HfModelFile>()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            if (item.getString("type") == "file") {
                                val fileName = item.getString("path")
                                val size = item.getLong("size")
                                val downloadUrl = "https://huggingface.co/$repoId/resolve/main/$fileName"
                                val format = if (fileName.endsWith(".gguf")) "gguf" else if (fileName.endsWith(".safetensors")) "safetensors" else "unknown"
                                val quantization = extractQuantization(fileName)
                                
                                files.add(HfModelFile(fileName, downloadUrl, size, quantization, format))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext files
    }

    override suspend fun filterCompatibleArtifacts(files: List<HfModelFile>): List<HfModelFile> {
        // We filter for compatible local formats. Mostly GGUF for now.
        return files.filter { it.format == "gguf" }
    }

    private fun extractRepoId(url: String): String? {
        val regex = Regex("huggingface\\.co/([^/]+/[^/]+)")
        val match = regex.find(url)
        return match?.groups?.get(1)?.value
    }
    
    private fun extractQuantization(fileName: String): String {
        val qRegex = Regex("(?i)q[0-9]_[kK]_[mM]|q[0-9]_[0-9]|f16")
        val match = qRegex.find(fileName)
        return match?.value?.uppercase() ?: "UNKNOWN"
    }
}
