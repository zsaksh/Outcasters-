import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

target = """                                            val systemContext = "You are Outcasters AI, a private local study assistant."
                                            val formattedPrompt = com.example.inference.ChatTemplateFormatter.formatSmolLM2(
                                                messages = history.takeLast(2) + com.example.backend.inference.ChatMessage("user", text),
                                                systemPrompt = systemContext
                                            )
                                            
                                            val responseFlow = inferenceEngine.generate(formattedPrompt)"""

replacement = """                                            val responseFlow = inferenceEngine.generate(routedPrompt.builtPrompt)"""

if target in text:
    text = text.replace(target, replacement)
else:
    print("Not found!")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
