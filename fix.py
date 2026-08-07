with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    text = f.read()
text = text.replace("data class Diagnostics(val tokensPerSecond: Float = 0f, val memoryUsageMb: Int = 0)\npackage", "package")
text = text.replace("class LlamaInferenceEngine", "data class Diagnostics(val tokensPerSecond: Float = 0f, val memoryUsageMb: Int = 0)\n\nclass LlamaInferenceEngine")
with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(text)
