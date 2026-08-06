package com.example.backend.inference

import com.example.backend.models.ModelManifest

data class ChatMessage(val role: String, val content: String)

class PromptBuilder : IPromptBuilder {
    
    override fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        systemContext: String?
    ): String {
        return when (manifest.chatTemplate.lowercase()) {
            "chatml" -> buildChatML(history, newTask, systemContext)
            "llama3" -> buildLlama3(history, newTask, systemContext)
            "phi3" -> buildPhi3(history, newTask, systemContext)
            "gemma" -> buildGemma(history, newTask, systemContext)
            "liquid" -> buildLiquid(history, newTask, systemContext)
            else -> buildFallback(history, newTask, systemContext)
        }
    }

    private fun buildGemma(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        val sb = StringBuilder()
        // Gemma 2 early quants often do not support a native system prompt, 
        // but if provided, it's typically pre-pended or appended to the first user turn.
        val contextPrefix = if (systemContext != null) "${systemContext}\n\n" else ""
        
        history.forEachIndexed { index, msg ->
            val roleTag = if (msg.role == "user") "user" else "model"
            sb.append("<start_of_turn>${roleTag}\n")
            if (index == 0 && msg.role == "user") {
                sb.append(contextPrefix)
            }
            sb.append("${msg.content}<end_of_turn>\n")
        }
        
        sb.append("<start_of_turn>user\n")
        if (history.isEmpty() && contextPrefix.isNotEmpty()) {
            sb.append(contextPrefix)
        }
        sb.append("${newTask}<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }

    private fun buildLiquid(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        // Liquid AI LFMs handling (often simplified ChatML or raw sequences)
        val sb = StringBuilder()
        if (systemContext != null) {
            sb.append("System: ${systemContext}\n\n")
        }
        history.forEach { msg ->
            val role = if (msg.role == "user") "User" else "Assistant"
            sb.append("${role}: ${msg.content}\n\n")
        }
        sb.append("User: ${newTask}\n\nAssistant: ")
        return sb.toString()
    }

    private fun buildChatML(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        val sb = StringBuilder()
        if (systemContext != null) {
            sb.append("<|im_start|>system\n${systemContext}<|im_end|>\n")
        }
        history.forEach { msg ->
            sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
        }
        sb.append("<|im_start|>user\n${newTask}<|im_end|>\n")
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildLlama3(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        val sb = StringBuilder()
        sb.append("<|begin_of_text|>")
        if (systemContext != null) {
            sb.append("<|start_header_id|>system<|end_header_id|>\n\n${systemContext}<|eot_id|>")
        }
        history.forEach { msg ->
            sb.append("<|start_header_id|>${msg.role}<|end_header_id|>\n\n${msg.content}<|eot_id|>")
        }
        sb.append("<|start_header_id|>user<|end_header_id|>\n\n${newTask}<|eot_id|>")
        sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
        return sb.toString()
    }

    private fun buildPhi3(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        val sb = StringBuilder()
        if (systemContext != null) {
            sb.append("<|system|>\n${systemContext}<|end|>\n")
        }
        history.forEach { msg ->
            val roleTag = if (msg.role == "user") "<|user|>" else "<|assistant|>"
            sb.append("${roleTag}\n${msg.content}<|end|>\n")
        }
        sb.append("<|user|>\n${newTask}<|end|>\n<|assistant|>\n")
        return sb.toString()
    }

    private fun buildFallback(history: List<ChatMessage>, newTask: String, systemContext: String?): String {
        val sb = StringBuilder()
        if (systemContext != null) {
            sb.append("SYSTEM: ${systemContext}\n")
        }
        history.forEach { msg ->
            sb.append("${msg.role.uppercase()}: ${msg.content}\n")
        }
        sb.append("USER: ${newTask}\nASSISTANT: ")
        return sb.toString()
    }
}
