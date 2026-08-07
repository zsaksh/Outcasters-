package com.example.data

import android.content.Context
import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.math.roundToInt

class HuggingFaceDownloader(private val context: Context) {
    private val client = HttpClient(Android)

    suspend fun downloadModel(url: String, fileName: String): Flow<Int> = flow {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            emit(100)
            return@flow
        }

        try {
            val response: HttpResponse = client.get(url)
            val contentLength = response.headers["Content-Length"]?.toLong() ?: 0L
            val channel: ByteReadChannel = response.bodyAsChannel()
            
            var bytesCopied = 0L
            val buffer = ByteArray(8 * 1024)
            file.outputStream().use { outputStream ->
                while (!channel.isClosedForRead) {
                    val read = channel.readAvailable(buffer)
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                        bytesCopied += read
                        if (contentLength > 0) {
                            val progress = ((bytesCopied.toFloat() / contentLength) * 100).roundToInt()
                            emit(progress)
                        }
                    }
                }
            }
            emit(100)
        } catch (e: Exception) {
            Log.e("HuggingFaceDownloader", "Download failed", e)
            file.delete()
            throw e
        }
    }.flowOn(Dispatchers.IO)

    fun getDownloadedModels(): List<File> {
        return context.filesDir.listFiles { _, name -> name.endsWith(".tflite") }?.toList() ?: emptyList()
    }
}
