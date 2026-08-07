sed -i 's/        lastSessionId = jobId/        val isNewSession = (lastSessionId != jobId) || (llmInference == null)\n        lastSessionId = jobId/g' app/src/main/java/com/example/inference/LlamaInferenceEngine.kt
sed -i 's/        val isNewSession = (lastSessionId != jobId) || (llmInference == null)//g' app/src/main/java/com/example/inference/LlamaInferenceEngine.kt
