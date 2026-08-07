package com.example.inference
import com.google.mediapipe.tasks.genai.llminference.LlmInference

fun test(llm: LlmInference) {
    llm.generateResponseAsync("test")
}
