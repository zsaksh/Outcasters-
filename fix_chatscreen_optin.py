import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

if "@OptIn(ExperimentalMaterial3Api::class)" not in content:
    content = content.replace("@Composable\nfun ChatScreen", "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun ChatScreen")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
