package com.example.inference

import com.example.backend.inference.ChatMessage

object ChatTemplateFormatter {
    fun formatSmolLM2(messages: List<ChatMessage>, systemPrompt: String): String {
        val sb = java.lang.StringBuilder()
        sb.append("<|im_start|>system\n${systemPrompt}<|im_end|>\n")
        for (msg in messages) {
            sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }
}
