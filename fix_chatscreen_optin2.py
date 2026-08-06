import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\n@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
