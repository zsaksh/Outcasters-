import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

replacement = """                                        try {
                                            isGenerating = true
                                            streamingResponse = ""
                                            val promptRouter = com.example.backend.inference.PromptRouter()
                                            val history = messages.map { com.example.backend.inference.ChatMessage(it.role, it.content) }
                                            val manifest = activeModel ?: com.example.backend.models.ModelManifest(modelId = "dummy", displayName = "Dummy", sourceUrl = "", fileName = "", chatTemplate = "fallback")
                                            val scannedText = savedStateHandle?.get<String>("scanned_text") ?: ""
                                            val routedPrompt = promptRouter.route(
                                                mode = currentMode,
                                                targetLanguage = currentTargetLanguage,
                                                manifest = manifest,
                                                history = history,
                                                newTask = text,
                                                ocrContext = scannedText,
                                                retrievalContext = ""
                                            )
                                            val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt)"""

content = re.sub(r'                                        try \{\n                                            isGenerating = true\n                                            streamingResponse = ""\n                                            val promptBuilder = com.example.backend.inference.PromptBuilder\(\)\n                                            val history = messages.map \{ com.example.backend.inference.ChatMessage\(it.role, it.content\) \}\n                                            val manifest = activeModel \?\: com.example.backend.models.ModelManifest\(modelId = "dummy", displayName = "Dummy", sourceUrl = "", fileName = "", chatTemplate = "fallback"\)\n                                            val builtPrompt = promptBuilder.buildPrompt\(manifest, history, text, currentMode, currentTargetLanguage\)\n                                            val responseFlow = inferenceEngine.generate\(builtPrompt\)', replacement, content)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
