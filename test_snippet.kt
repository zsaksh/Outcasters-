package com.example

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.lang.reflect.Method

fun main() {
    val methods = LlmInference::class.java.methods
    methods.forEach { println(it.name) }
}
