import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

content = content.replace('val scannedText = savedStateHandle?.get<String>("scanned_text") ?: ""', 'val scannedText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scanned_text") ?: ""')

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
