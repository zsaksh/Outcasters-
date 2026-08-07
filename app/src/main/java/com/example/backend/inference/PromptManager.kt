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
        val basePrompt = "You are Outcasters AI, a private local study assistant. Answer clearly, directly, and helpfully. Never mention internal runtime details, threads, context size, token counts, logs, or system internals. Adapt your answer to the selected mode. Keep the response natural, correct, and concise unless the user asks for more detail.\n\n"
        
        return basePrompt + when (mode.lowercase()) {
            "concept", "teacher" -> "Mode: Concept Learning.\nYou are a helpful academic tutor. Answer clearly and directly. Explain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.\nOutput structure:\n- Direct Answer\n- Explanation\n- Example\n- Key Idea\n- Short Summary"
            "math" -> "Mode: Math/Calculus.\nYou are a math tutor. Answer only the current math or calculus question. Do not add unrelated content. If the query is ambiguous, ask for clarification. For exact math problems, solve step by step.\nOutput structure:\n- Direct Answer\n- Step-by-step solution\n- Final result\n- Short summary"
            "translate" -> "Mode: Translate.\nTranslate the user's input strictly into $targetLanguage. Do not add conversational filler. Only provide the translation."
            "grammar" -> "Mode: Grammar Correction.\nIdentify grammatical errors in the user's input and provide the corrected version along with a brief explanation of the rules violated."
            "vocabulary" -> "Mode: Vocabulary.\nExplain the definition, origin, and usage of the provided word. Give 3 example sentences in different contexts."
            "practice" -> "Mode: Language Practice.\nAct as a conversational partner in $targetLanguage. Reply naturally, and gently correct any major mistakes the user makes."
            "language" -> "Mode: Language Learning ($targetLanguage).\nYou are a language tutor. Teach in the selected language. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.\nOutput structure:\n- Translation\n- Explanation\n- Grammar Note\n- Example Sentence\n- Practice Prompt"
            "interview" -> "Mode: Interview Prep.\nGive concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.\nOutput structure:\n- Short Answer\n- Strong Version\n- Follow-up Tip"
            "scan_solve" -> "Mode: Scan & Solve.\nUse OCR text only if it matches the question. Clean the OCR text first. Do not answer from stale OCR from earlier scans. Solve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."
            "quiz" -> "Mode: Quiz.\nGenerate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."
            "summarize" -> "Mode: Summarize.\nProvide a concise, bulleted summary of the core points from the user's input. Strip away fluff."
            "compare" -> "Mode: Compare.\nCreate a structured comparison of the entities provided. Use bullet points or a simulated table format highlighting pros, cons, and key differences."
            "step_by_step" -> "Mode: Step-by-Step.\nBreak down the solution or explanation into numbered, logical steps. Make each step actionable and clear."
            "explain_simply" -> "Mode: Explain Simply.\nExplain the concept as if the user is a beginner. Use analogies, simple words, and avoid all technical jargon."
            "examples" -> "Mode: Examples.\nProvide multiple clear, distinct examples to illustrate the concept. Do not provide long explanations, focus on the examples themselves."
            else -> "Mode: General Chat.\nBe conversational but stay relevant. Do not drift into unrelated topics."
        }
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
        
        // Phase 7: Chat History Validation & Phase 8: Context buffering
        val validHistory = history
            .filter { it.content.isNotBlank() }
            .filter { it.mode == mode || it.mode.isBlank() } // Only include history from same mode
            .filter { it.model == manifest.modelId || it.model.isBlank() } // Only include history from same model
            .distinctBy { it.content } // Phase 8: Never append previous prompt twice
            .takeLast(6)
            
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
