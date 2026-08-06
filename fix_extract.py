import re

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    text = f.read()

# Fix the string extraction
text = text.replace('val newTaskLower = lowerPrompt.substringAfterLast("<|im_start|>user\\n").substringAfterLast("user\\n").substringAfterLast("user:").substringAfterLast("<|user|>").substringAfterLast("<|start_header_id|>user<|end_header_id|>")',
'''val extracted = lowerPrompt.substringAfterLast("<|im_start|>user\\n").substringAfterLast("user\\n").substringAfterLast("user:").substringAfterLast("<|user|>").substringAfterLast("<|start_header_id|>user<|end_header_id|>")
        val newTaskLower = extracted.substringBefore("<|im_end|>").substringBefore("<|end|>").substringBefore("<|eot_id|>").substringBefore("assistant").trim()''')


with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(text)
