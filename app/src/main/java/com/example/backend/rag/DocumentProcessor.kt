package com.example.backend.rag

import android.util.Log

class DocumentProcessor {
    fun processText(text: String, chunkSize: Int = 500, overlap: Int = 50): List<String> {
        Log.d("DocumentProcessor", "Chunking text of length ${text.length}")
        val words = text.split(Regex("\\s+"))
        val chunks = mutableListOf<String>()
        
        var i = 0
        while (i < words.size) {
            val end = (i + chunkSize).coerceAtMost(words.size)
            val chunk = words.subList(i, end).joinToString(" ")
            chunks.add(chunk)
            i += (chunkSize - overlap)
        }
        return chunks
    }
}
