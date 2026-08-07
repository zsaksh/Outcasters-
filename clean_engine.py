import re

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    text = f.read()

# Remove duplicate block of variable declarations
dup_block = """        val isAi = newTaskLower.contains("what is ai")
        val isTranslate = newTaskLower.contains("translate hello to french")
        val isGravity = newTaskLower.contains("define gravity")
        val isQuizMe = newTaskLower.contains("quiz me")
        val isSummarize = newTaskLower.contains("summarize")
        val isCompare = newTaskLower.contains("compare java vs kotlin")"""

text = text.replace(dup_block, "", 1) # Replace only the first occurrence

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(text)
