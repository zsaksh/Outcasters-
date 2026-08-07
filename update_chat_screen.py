import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
    'val history = messages.map { com.example.backend.inference.ChatMessage(it.role, it.content) }',
    'val history = messages.map { com.example.backend.inference.ChatMessage(role = it.role, content = it.content, mode = currentMode, model = activeModel?.modelId ?: "", language = currentTargetLanguage) }'
)

old_flow_logic = '''                                            llamaBridge.clearKvCache()
                                            
                                            val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt, routedPrompt.config)
                                            var fullResponse = ""
                                            responseFlow.collect { word -> 
                                                fullResponse += word 
                                                streamingResponse = fullResponse 
                                            }
                                            
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = postProcessor.cleanResponse(fullResponse), timestamp = System.currentTimeMillis())
                                            )'''

new_flow_logic = '''                                            llamaBridge.clearKvCache()
                                            
                                            var isValid = false
                                            var attempts = 0
                                            var finalResponse = ""
                                            
                                            while (!isValid && attempts < 2) {
                                                val jobId = java.util.UUID.randomUUID().toString()
                                                val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt, routedPrompt.config, jobId)
                                                var fullResponse = ""
                                                responseFlow.collect { word -> 
                                                    fullResponse += word 
                                                    streamingResponse = fullResponse 
                                                }
                                                
                                                val cleanedResponse = postProcessor.cleanResponse(fullResponse)
                                                isValid = postProcessor.validateQuality(text, cleanedResponse, routedPrompt.effectiveMode)
                                                finalResponse = cleanedResponse
                                                attempts++
                                            }
                                            
                                            if (finalResponse.isNotBlank()) {
                                                chatDao.insertMessage(
                                                    ChatMessageEntity(sessionId = sId, role = "model", content = finalResponse, timestamp = System.currentTimeMillis())
                                                )
                                            }'''

text = text.replace(old_flow_logic, new_flow_logic)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
