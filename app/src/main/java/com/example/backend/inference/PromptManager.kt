package com.example.backend.inference

import com.example.backend.models.ModelManifest
import android.util.Log

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val conversationId: String = "",
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "",
    val mode: String = "",
    val language: String = "",
    val tokenCount: Int = 0
)

object PromptManager : IPromptBuilder {

    private fun getSystemContext(mode: String, targetLanguage: String): String {
        return "You are an intelligent AI assistant. Provide clear, concise, and helpful answers."
    }

    override fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        mode: String,
        targetLanguage: String
    ): String {
        Log.i("PromptManager", "Building prompt for mode: $mode, language: $targetLanguage, model: ${manifest.modelId}")
        val systemContext = getSystemContext(mode, targetLanguage)
        
        val validHistory = history
            .filter { it.content.isNotBlank() }
            .takeLast(2)
            
        return when (manifest.chatTemplate.lowercase()) {
            "chatml" -> buildChatML(validHistory, newTask, systemContext)
            "llama3" -> buildLlama3(validHistory, newTask, systemContext)
            "phi3" -> buildPhi3(validHistory, newTask, systemContext)
            "gemma" -> buildGemma(validHistory, newTask, systemContext)
            "liquid" -> buildLiquid(validHistory, newTask, systemContext)
            else -> buildFallback(validHistory, newTask, systemContext)
        }
    }

    private fun buildGemma(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        val contextPrefix = "${systemContext}\n\n"
        
        history.forEachIndexed { index, msg ->
            val roleTag = if (msg.role == "user") "user" else "model"
            sb.append("<start_of_turn>${roleTag}\n")
            if (index == 0 && msg.role == "user") {
                sb.append(contextPrefix)
            }
            sb.append("${msg.content}<end_of_turn>\n")
        }
        
        sb.append("<start_of_turn>user\n")
        if (history.isEmpty()) {
            sb.append(contextPrefix)
        }
        sb.append("${newTask}<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }

    private fun buildLiquid(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("System: ${systemContext}\n\n")
        
        history.forEach { msg ->
            val role = if (msg.role == "user") "User" else "Assistant"
            sb.append("${role}: ${msg.content}\n\n")
        }
        sb.append("User: ${newTask}\n\nAssistant: ")
        return sb.toString()
    }

    private fun buildChatML(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\n${systemContext}<|im_end|>\n")
        
        history.forEach { msg ->
            sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
        }
        sb.append("<|im_start|>user\n${newTask}<|im_end|>\n")
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildLlama3(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|begin_of_text|>")
        sb.append("<|start_header_id|>system<|end_header_id|>\n\n${systemContext}<|eot_id|>")
        
        history.forEach { msg ->
            sb.append("<|start_header_id|>${msg.role}<|end_header_id|>\n\n${msg.content}<|eot_id|>")
        }
        sb.append("<|start_header_id|>user<|end_header_id|>\n\n${newTask}<|eot_id|>")
        sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
        return sb.toString()
    }

    private fun buildPhi3(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("<|system|>\n${systemContext}<|end|>\n")
        
        history.forEach { msg ->
            val roleTag = if (msg.role == "user") "<|user|>" else "<|assistant|>"
            sb.append("${roleTag}\n${msg.content}<|end|>\n")
        }
        sb.append("<|user|>\n${newTask}<|end|>\n<|assistant|>\n")
        return sb.toString()
    }

    private fun buildFallback(history: List<ChatMessage>, newTask: String, systemContext: String): String {
        val sb = StringBuilder()
        sb.append("SYSTEM: ${systemContext}\n")
        
        history.forEach { msg ->
            sb.append("${msg.role.uppercase()}: ${msg.content}\n")
        }
        sb.append("USER: ${newTask}\nASSISTANT: ")
        return sb.toString()
    }
}
