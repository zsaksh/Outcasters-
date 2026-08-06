with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Remove the previous single one to avoid duplicates
text = text.replace("@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ChatScreen", "@Composable\nfun ChatScreen")

# Add it to all composables
text = text.replace("@Composable\nfun", "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
