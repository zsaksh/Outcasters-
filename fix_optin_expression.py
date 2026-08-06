import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Fix the TopAppBar and ModalBottomSheet expressions
text = re.sub(r'@androidx\.compose\.material3\.ExperimentalMaterial3Api\s*TopAppBar\(', 'TopAppBar(', text)
text = re.sub(r'@androidx\.compose\.material3\.ExperimentalMaterial3Api\s*ModalBottomSheet\(', 'ModalBottomSheet(', text)

# Just clean up any OptIn usage on expressions
text = text.replace("@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)TopAppBar", "TopAppBar")
text = text.replace("@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\nTopAppBar", "TopAppBar")
text = text.replace("@androidx.compose.material3.ExperimentalMaterial3ApiTopAppBar", "TopAppBar")
text = text.replace("@androidx.compose.material3.ExperimentalMaterial3Api\nTopAppBar", "TopAppBar")
text = text.replace("@androidx.compose.material3.ExperimentalMaterial3ApiModalBottomSheet", "ModalBottomSheet")
text = text.replace("@androidx.compose.material3.ExperimentalMaterial3Api\nModalBottomSheet", "ModalBottomSheet")


with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
