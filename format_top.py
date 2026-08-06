with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Replace any malformed package/import combinations
import re
text = re.sub(r'package com\.example\.ui\.screens(.*?)(import)', r'package com.example.ui.screens\n\1\n\2', text, count=1)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
