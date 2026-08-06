package com.example.backend.hf

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.OutcastersApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo

class HFDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("download_channel", "Model Downloads", NotificationManager.IMPORTANCE_LOW)
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, "download_channel")
            .setContentTitle("Downloading Model")
            .setContentText("Preparing download...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1001, notification)
        }
        setForeground(foregroundInfo)

        val modelId = inputData.getString("MODEL_ID") ?: return@withContext Result.failure()
        val downloadUrl = inputData.getString("DOWNLOAD_URL") ?: return@withContext Result.failure()
        val fileName = inputData.getString("FILE_NAME") ?: return@withContext Result.failure()
        
        val db = androidx.room.Room.databaseBuilder(applicationContext, com.example.data.SrsDatabase::class.java, "srs_db").fallbackToDestructiveMigration().build()
        val dao = db.modelManifestDao()
        
        try {
            val model = dao.getModelById(modelId) ?: return@withContext Result.failure()
            dao.update(model.copy(downloadStatus = "downloading", installStatus = "downloading"))

            val modelsDir = File(applicationContext.filesDir, "models")
            if (!modelsDir.exists()) modelsDir.mkdirs()
            
            val outputFile = File(modelsDir, fileName)
            var downloadedBytes = 0L
            
            if (outputFile.exists()) {
                downloadedBytes = outputFile.length()
            }
            
            val requestBuilder = Request.Builder().url(downloadUrl)
            if (downloadedBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
            }
            
            val request = requestBuilder.build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    dao.update(model.copy(downloadStatus = "failed", errorState = "HTTP ${response.code}"))
                    return@withContext Result.failure()
                }
                
                val body = response.body ?: return@withContext Result.failure()
                val totalBytes = downloadedBytes + body.contentLength()
                
                // Update manifest with total size if 0
                if (model.fileSizeBytes == 0L) {
                    dao.update(model.copy(fileSizeBytes = totalBytes))
                }
                
                RandomAccessFile(outputFile, "rw").use { raf ->
                    raf.seek(downloadedBytes)
                    
                    val inputStream = body.byteStream()
                    val buffer = ByteArray(8192)
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()
                    
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        if (isStopped) {
                            // Work was cancelled/paused
                            dao.update(model.copy(downloadStatus = "paused"))
                            return@withContext Result.retry()
                        }
                        
                        raf.write(buffer, 0, read)
                        downloadedBytes += read
                        
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 1000) {
                            lastUpdate = now
                            setProgress(workDataOf("PROGRESS" to (downloadedBytes.toFloat() / totalBytes * 100).toInt()))
                            val progress = (downloadedBytes.toFloat() / totalBytes * 100).toInt()
                            val updateNotification = NotificationCompat.Builder(applicationContext, "download_channel")
                                .setContentTitle("Downloading Model")
                                .setContentText("Progress: $progress%")
                                .setProgress(100, progress, false)
                                .setSmallIcon(android.R.drawable.stat_sys_download)
                                .setOngoing(true)
                                .build()
                            val foregroundInfoUpdate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                ForegroundInfo(1001, updateNotification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                            } else {
                                ForegroundInfo(1001, updateNotification)
                            }
                            setForeground(foregroundInfoUpdate)

                        }
                    }
                }
            }
            
            // Finished successfully
            dao.update(model.copy(downloadStatus = "downloaded", installStatus = "ready"))
            
            // Auto-activate logic
            val settingsManager = com.example.backend.settings.SettingsManager(applicationContext)
            val autoActivate = settingsManager.autoActivateModels.first()
            if (autoActivate) {
                dao.setActiveModelAtomic(modelId)
            }
            
            return@withContext Result.success()
            
        } catch (e: Exception) {
            Log.e("HFDownloadWorker", "Download failed", e)
            return@withContext Result.retry()
        }
    }
}
