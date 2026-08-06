with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

import re
text = re.sub(r'package com\.example\.ui\.screens', '', text)
text = re.sub(r'@file:OptIn\(androidx\.compose\.material3\.ExperimentalMaterial3Api::class\)', '', text)
# remove any stray newlines at the very beginning
text = text.lstrip()

# Now find all imports and put newlines before them properly
# But wait, they might be jammed together like import Aimport B
text = text.replace("import ", "\nimport ")
text = text.replace("fun ", "\nfun ")
text = text.replace("@Composable", "\n@Composable")

# Add the header
text = "package com.example.ui.screens\n\n@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n\n" + text

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
