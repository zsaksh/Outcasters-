import re
with open("app/build.gradle.kts", "r") as f:
    text = f.read()

# Add freeCompilerArgs to kotlinOptions
if "kotlinOptions {" in text and "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api" not in text:
    text = text.replace("kotlinOptions {", "kotlinOptions {\n        freeCompilerArgs += listOf(\"-opt-in=androidx.compose.material3.ExperimentalMaterial3Api\")")

with open("app/build.gradle.kts", "w") as f:
    f.write(text)
