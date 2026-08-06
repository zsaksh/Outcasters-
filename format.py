import re
with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Strip all package and optin stuff
text = re.sub(r'package com\.example\.ui\.screens', '', text)
text = re.sub(r'@file:OptIn\(androidx\.compose\.material3\.ExperimentalMaterial3Api::class\)', '', text)
# find the first import
first_import_idx = text.find('import ')
if first_import_idx != -1:
    before = text[:first_import_idx].strip()
    after = text[first_import_idx:]
    text = f"package com.example.ui.screens\n\n@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n\n{after}"

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
