import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

retry_logic = """val llamaBridge = com.example.inference.LlamaBridge()
                                            
                                            var retryCount = 0
                                            var initSuccess = false
                                            while (retryCount < 3 && !initSuccess) {
                                                try {
                                                    if (!llamaBridge.isModelReady()) {
                                                        val path = activeModel?.fileName ?: "default.gguf"
                                                        llamaBridge.loadModel(path)
                                                    }
                                                    initSuccess = llamaBridge.isModelReady()
                                                } catch (e: Exception) {
                                                    // Catch null pointer or initialization errors and retry
                                                }
                                                if (!initSuccess) {
                                                    retryCount++
                                                    kotlinx.coroutines.delay(100)
                                                }
                                            }
                                            
                                            if (!initSuccess) {
                                                throw IllegalStateException("Failed to initialize C++ inference engine after retries.")
                                            }
                                            
                                            llamaBridge.clearKvCache()"""

text = text.replace('val llamaBridge = com.example.inference.LlamaBridge()\n                                            llamaBridge.clearKvCache()', retry_logic)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
