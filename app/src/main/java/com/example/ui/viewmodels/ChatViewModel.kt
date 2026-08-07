package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.data.ChatMessageEntity
import com.example.backend.inference.ChatMessage
import com.example.backend.inference.InferenceLogger
import com.example.inference.LlamaInferenceEngine
import com.example.backend.inference.PostProcessor
import com.example.backend.models.ModelManifest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class ChatViewModel(
    private val repository: ChatRepository,
    private val inferenceEngine: LlamaInferenceEngine,
    private val inferenceLogger: InferenceLogger
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val postProcessor = PostProcessor()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(
        content: String,
        sessionId: Long,
        mode: String,
        targetLanguage: String,
        manifest: ModelManifest
    ) {
        if (content.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            val userMsg = ChatMessage(
                conversationId = sessionId.toString(),
                role = "user",
                content = content,
                timestamp = System.currentTimeMillis(),
                model = manifest.modelId,
                mode = mode,
                language = targetLanguage,
                tokenCount = 0
            )
            repository.insertMessage(userMsg)

            _isGenerating.value = true
            _streamingResponse.value = ""
            var fullResponse = ""

            val startTime = System.currentTimeMillis()
            var tokenCount = 0

            try {
                val history = repository.getMessagesForSession(sessionId).first()

                inferenceEngine.generate(
                    newTask = content,
                    history = history,
                    mode = mode,
                    targetLanguage = targetLanguage,
                    manifest = manifest
                ).collect { chunk ->
                    if (chunk.startsWith("Error:")) {
                        throw RuntimeException(chunk)
                    }
                    fullResponse += chunk
                    _streamingResponse.value = fullResponse
                    tokenCount++ // Approximating token count
                }

                val latencyMs = System.currentTimeMillis() - startTime
                val memoryUsageMb = inferenceEngine.diagnostics.value.memoryUsageMb

                inferenceLogger.logMetrics(
                    sessionId = sessionId,
                    latencyMs = latencyMs,
                    tokenCount = tokenCount,
                    memoryUsageMb = memoryUsageMb,
                    model = manifest.modelId
                )

                val modelMsg = ChatMessage(
                    conversationId = sessionId.toString(),
                    role = "model",
                    content = postProcessor.cleanResponse(fullResponse),
                    timestamp = System.currentTimeMillis(),
                    model = manifest.modelId,
                    mode = mode,
                    language = targetLanguage,
                    tokenCount = tokenCount
                )
                repository.insertMessage(modelMsg)

            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    conversationId = sessionId.toString(),
                    role = "model",
                    content = "Error: Local model is not loaded or crashed. ${e.message}",
                    timestamp = System.currentTimeMillis(),
                    model = manifest.modelId,
                    mode = mode,
                    language = targetLanguage,
                    tokenCount = 0
                )
                repository.insertMessage(errorMsg)
            } finally {
                _isGenerating.value = false
                _streamingResponse.value = ""
            }
        }
    }
}
