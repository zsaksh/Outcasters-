with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

import re
text = re.sub(r'@file:OptIn\(androidx\.compose\.material3\.ExperimentalMaterial3Api::class\)', '', text)
text = text.replace("@Composable\nfun ChatScreen", "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun ChatScreen")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
