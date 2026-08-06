import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

replacement = """                                        try {
                                            isGenerating = true
                                            streamingResponse = ""
                                            val promptRouter = com.example.backend.inference.PromptRouter()
                                            val postProcessor = com.example.backend.inference.PostProcessor()
                                            val history = messages.map { com.example.backend.inference.ChatMessage(it.role, it.content) }
                                            val manifest = activeModel ?: com.example.backend.models.ModelManifest(modelId = "dummy", displayName = "Dummy", sourceUrl = "", fileName = "", chatTemplate = "fallback")
                                            val scannedText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scanned_text") ?: ""
                                            val routedPrompt = promptRouter.route(
                                                mode = currentMode,
                                                targetLanguage = currentTargetLanguage,
                                                manifest = manifest,
                                                history = history,
                                                newTask = text,
                                                ocrContext = scannedText,
                                                retrievalContext = ""
                                            )
                                            val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt)
                                            var fullResponse = ""
                                            responseFlow.collect { word -> 
                                                fullResponse += word 
                                                streamingResponse = postProcessor.cleanResponse(fullResponse)
                                            }
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = postProcessor.cleanResponse(fullResponse), timestamp = System.currentTimeMillis())
                                            )
                                        }"""

content = re.sub(r'                                        try \{\n                                            isGenerating = true\n                                            streamingResponse = ""\n                                            val promptRouter = com.example.backend.inference.PromptRouter\(\).*?                                            chatDao.insertMessage\(\n                                                ChatMessageEntity\(sessionId = sId, role = "model", content = fullResponse, timestamp = System.currentTimeMillis\(\)\)\n                                            \)\n                                        \}', replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
