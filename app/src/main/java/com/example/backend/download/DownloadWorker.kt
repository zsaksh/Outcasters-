package com.example.backend.download

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadUrl = inputData.getString("DOWNLOAD_URL") ?: return@withContext Result.failure()
        val fileName = inputData.getString("FILE_NAME") ?: return@withContext Result.failure()
        val expectedChecksum = inputData.getString("EXPECTED_CHECKSUM") ?: ""
        
        Log.d("DownloadWorker", "Starting download for \$fileName from \$downloadUrl")
        
        val outputFile = File(applicationContext.filesDir, fileName)
        
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection()
            connection.connect()
            
            val fileLength = connection.contentLength
            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(outputFile)
            
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            
            while (inputStream.read(data).also { count = it } != -1) {
                if (isStopped) {
                    outputStream.close()
                    inputStream.close()
                    outputFile.delete() // Clean up partial download
                    return@withContext Result.failure()
                }
                
                total += count
                outputStream.write(data, 0, count)
                
                // Report progress
                val progress = if (fileLength > 0) ((total * 100) / fileLength).toInt() else 0
                setProgress(workDataOf("PROGRESS" to progress))
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            // Verify checksum
            if (expectedChecksum.isNotEmpty()) {
                val actualChecksum = calculateSha256(outputFile)
                if (actualChecksum != expectedChecksum) {
                    Log.e("DownloadWorker", "Checksum mismatch. Expected \$expectedChecksum, got \$actualChecksum")
                    outputFile.delete()
                    return@withContext Result.failure(workDataOf("ERROR" to "Checksum mismatch"))
                }
            }
            
            Log.d("DownloadWorker", "Download completed successfully: \$fileName")
            return@withContext Result.success(workDataOf("FILE_PATH" to outputFile.absolutePath))
            
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error downloading file: \${e.message}")
            if (outputFile.exists()) {
                outputFile.delete() // Atomic failure recovery: clean up partial files
            }
            return@withContext Result.failure(workDataOf("ERROR" to e.message))
        }
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead = fis.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = fis.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
