import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Try to insert newlines correctly
if "package com.example.ui.screens@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)import" in text:
    text = text.replace("package com.example.ui.screens@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)import", "package com.example.ui.screens\n\n@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n\nimport")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
