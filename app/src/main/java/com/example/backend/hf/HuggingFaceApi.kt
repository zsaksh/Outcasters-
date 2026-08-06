package com.example.backend.hf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class HFModelInfo(
    val id: String,
    val author: String,
    val downloads: Int,
    val likes: Int
)

data class HFFileInfo(
    val path: String,
    val size: Long,
    val downloadUrl: String
)

class HuggingFaceApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun searchGGUFModels(query: String = ""): List<HFModelInfo> = withContext(Dispatchers.IO) {
        val url = if (query.isNotBlank()) {
            "https://huggingface.co/api/models?search=$query&filter=gguf,text-generation&sort=downloads&direction=-1&limit=20"
        } else {
            "https://huggingface.co/api/models?filter=gguf,text-generation&sort=downloads&direction=-1&limit=20"
        }
        
        val request = Request.Builder().url(url).build()
        val models = mutableListOf<HFModelInfo>()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@use
                    val jsonArray = JSONArray(responseBody)
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        models.add(
                            HFModelInfo(
                                id = obj.optString("id", ""),
                                author = obj.optString("author", "Unknown"),
                                downloads = obj.optInt("downloads", 0),
                                likes = obj.optInt("likes", 0)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext models
    }

    suspend fun getModelFiles(repoId: String): List<HFFileInfo> = withContext(Dispatchers.IO) {
        // repoId example: bartowski/Qwen2.5-3B-Instruct-GGUF
        val url = "https://huggingface.co/api/models/$repoId/tree/main"
        val request = Request.Builder().url(url).build()
        val files = mutableListOf<HFFileInfo>()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@use
                    val jsonArray = JSONArray(responseBody)
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val type = obj.optString("type")
                        val path = obj.optString("path")
                        
                        if (type == "file" && path.endsWith(".gguf", ignoreCase = true)) {
                            files.add(
                                HFFileInfo(
                                    path = path,
                                    size = obj.optLong("size", 0L),
                                    downloadUrl = "https://huggingface.co/$repoId/resolve/main/$path"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext files
    }
}
