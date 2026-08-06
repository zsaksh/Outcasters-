with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Make sure we add OptIn to the TopAppBar and ModalBottomSheet or just the whole ChatScreen
text = text.replace("@Composable\nfun ChatScreen", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ChatScreen")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
