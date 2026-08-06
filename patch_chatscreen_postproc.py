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
                                            responseFlow.collect { chunk ->
                                                fullResponse += chunk
                                                streamingResponse = postProcessor.cleanResponse(fullResponse)
                                            }
                                            
                                            if (sId == null) {
                                                val newId = chatDao.insertSession(
                                                    com.example.data.ChatSessionEntity(
                                                        title = text.take(20) + if (text.length > 20) "..." else "",
                                                        timestamp = System.currentTimeMillis(),
                                                        mode = currentMode
                                                    )
                                                )
                                                sId = newId
                                                navController.currentBackStackEntry?.savedStateHandle?.set("session_id", newId)
                                            }
                                            
                                            if (sId != null) {
                                                chatDao.insertMessage(
                                                    com.example.data.ChatMessageEntity(
                                                        sessionId = sId!!,
                                                        role = "user",
                                                        content = text,
                                                        timestamp = System.currentTimeMillis() - 1000
                                                    )
                                                )
                                                chatDao.insertMessage(
                                                    com.example.data.ChatMessageEntity(
                                                        sessionId = sId!!,
                                                        role = "assistant",
                                                        content = postProcessor.cleanResponse(fullResponse),
                                                        timestamp = System.currentTimeMillis()
                                                    )
                                                )
                                            }"""

content = re.sub(r'                                        try \{\n                                            isGenerating = true.*?                                            \}\n                                            \}\n', replacement + '\n', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
