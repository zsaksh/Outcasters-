package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OcrPipeline(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromUri(uri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error extracting text"
        }
    }
    
    suspend fun extractTextFromBitmap(bitmap: Bitmap, cropRect: android.graphics.Rect? = null): String {
        return try {
            val finalBitmap = if (cropRect != null) {
                // Ensure rect is within bounds
                val safeLeft = Math.max(0, cropRect.left)
                val safeTop = Math.max(0, cropRect.top)
                val safeWidth = Math.min(bitmap.width - safeLeft, cropRect.width())
                val safeHeight = Math.min(bitmap.height - safeTop, cropRect.height())
                
                if (safeWidth > 0 && safeHeight > 0) {
                    Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeWidth, safeHeight)
                } else {
                    bitmap
                }
            } else {
                bitmap
            }
            val image = InputImage.fromBitmap(finalBitmap, 0)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error extracting text from cropped region"
        }
    }

    suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext "Error reading PDF"
            val renderer = PdfRenderer(pfd)
            val sb = StringBuilder()
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                // Render at a higher resolution for ML Kit OCR 
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()
                sb.append("--- Page ${i + 1} ---\n")
                sb.append(result.text).append("\n")
                page.close()
            }
            renderer.close()
            pfd.close()
            sb.toString()
        } catch(e: Exception) {
            e.printStackTrace()
            "Failed to extract PDF"
        }
    }
}
