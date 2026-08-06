package com.example.backend.inference

import com.example.backend.models.ModelManifest

data class ChatMessage(val role: String, val content: String)

class PromptBuilder : IPromptBuilder {
    
    private fun getSystemContext(mode: String, targetLanguage: String): String {
        val basePrompt = "You are Outcasters AI, a private local study assistant. Answer clearly, directly, and helpfully. Never mention internal runtime details, threads, context size, token counts, logs, or system internals. Adapt your answer to the selected mode. Keep the response natural, correct, and concise unless the user asks for more detail.\n\n"
        
        return basePrompt + when (mode.lowercase()) {
            "concept" -> "Mode: Concept Learning.\nExplain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.\nOutput structure:\n- Direct Answer\n- Explanation\n- Example\n- Key Idea\n- Short Summary"
            "language", "translate", "vocabulary", "grammar", "practice" -> "Mode: Language Learning ($targetLanguage).\nBehave like a language tutor. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.\nOutput structure:\n- Translation\n- Explanation\n- Grammar Note\n- Example Sentence\n- Practice Prompt"
            "interview" -> "Mode: Interview Prep.\nGive concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.\nOutput structure:\n- Short Answer\n- Strong Version\n- Follow-up Tip"
            "scan_solve" -> "Mode: Scan & Solve.\nSolve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."
            "quiz" -> "Mode: Quiz.\nGenerate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."
            else -> "Mode: General Chat.\nBe conversational, helpful, and concise."
        }
    }
    
    override fun buildPrompt(
        manifest: ModelManifest, 
        history: List<ChatMessage>, 
        newTask: String,
        mode: String,
        targetLanguage: String
    ): String {
        val systemContext = getSystemContext(mode, targetLanguage)
        
        return when (manifest.chatTemplate.lowercase()) {
            "chatml" -> buildChatML(history, newTask, systemContext)
            "llama3" -> buildLlama3(history, newTask, systemContext)
            "phi3" -> buildPhi3(history, newTask, systemContext)
            "gemma" -> buildGemma(history, newTask, systemContext)
            "liquid" -> buildLiquid(history, newTask, systemContext)
            else -> buildFallback(history, newTask, systemContext)
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
