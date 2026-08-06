package com.example.backend.hf

import android.content.Context
import kotlinx.coroutines.flow.first
import android.util.Log
import com.example.backend.models.ModelManifest
import com.example.backend.models.ModelManifestDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadManager(
    private val context: Context,
    private val dao: ModelManifestDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun startDownload(modelId: String) {
        if (downloadJobs.containsKey(modelId)) return // Already downloading

        val model = dao.getModelById(modelId) ?: return
        dao.update(model.copy(installStatus = "downloading"))

        val job = scope.launch {
            try {
                executeDownload(model)
            } catch (e: CancellationException) {
                Log.d("DownloadManager", "Download paused/cancelled for $modelId")
                // Keep the state updated in pause() method
            } catch (e: Exception) {
                Log.e("DownloadManager", "Download failed for $modelId", e)
                val current = dao.getModelById(modelId)
                if (current != null) {
                    dao.update(current.copy(installStatus = "failed", errorState = e.message ?: "Unknown error"))
                }
            } finally {
                downloadJobs.remove(modelId)
            }
        }
        downloadJobs[modelId] = job
    }

    suspend fun pauseDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        val model = dao.getModelById(modelId)
        if (model != null && model.installStatus == "downloading") {
            dao.update(model.copy(installStatus = "paused"))
        }
    }
    
    suspend fun retryDownload(modelId: String) {
        startDownload(modelId)
    }

    private suspend fun executeDownload(model: ModelManifest) {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()

        val outputFile = File(modelsDir, model.fileName)
        var downloadedBytes = if (outputFile.exists()) outputFile.length() else 0L

        val requestBuilder = Request.Builder().url(model.sourceUrl)
        if (downloadedBytes > 0) {
            requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
        }
        
        val request = requestBuilder.build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    throw Exception("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw Exception("Empty response body")
                val totalBytes = downloadedBytes + body.contentLength()

                // If fileSizeBytes in manifest is 0, update it now
                if (model.fileSizeBytes == 0L || model.fileSizeBytes != totalBytes) {
                    dao.update(dao.getModelById(model.modelId)!!.copy(fileSizeBytes = totalBytes))
                }

                RandomAccessFile(outputFile, "rw").use { raf ->
                    raf.seek(downloadedBytes)

                    val inputStream = body.byteStream()
                    val buffer = ByteArray(8192 * 4) // 32KB buffer for speed
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        yield() // Check for cancellation
                        raf.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 1000) { // Update DB once per second
                            lastUpdate = now
                            val progress = if (totalBytes > 0) ((downloadedBytes.toFloat() / totalBytes) * 100).toInt() else 0
                            dao.update(dao.getModelById(model.modelId)!!.copy(downloadProgress = progress))
                        }
                    }
                }
            }
        }

        // Verification phase
        var latestModel = dao.getModelById(model.modelId) ?: return
        dao.update(latestModel.copy(installStatus = "verifying", downloadProgress = 100))
        
        // Simple verification (just checking if file exists and size matches)
        val finalFile = File(modelsDir, latestModel.fileName)
        if (finalFile.exists() && finalFile.length() == latestModel.fileSizeBytes) {
            latestModel = dao.getModelById(model.modelId) ?: return
            dao.update(latestModel.copy(installStatus = "ready", lastVerifiedTime = System.currentTimeMillis()))
            
            // Auto-activate logic
            val settingsManager = com.example.backend.settings.SettingsManager(context)
            val autoActivate = settingsManager.autoActivateModels.first()
            if (autoActivate) {
                dao.setActiveModelAtomic(model.modelId)
            }
        } else {
            latestModel = dao.getModelById(model.modelId) ?: return
            dao.update(latestModel.copy(installStatus = "corrupted", errorState = "File size mismatch after download"))
        }
    }
}
