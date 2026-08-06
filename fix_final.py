with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Just put a newline before EVERY import
text = text.replace("import", "\nimport")
text = text.replace("\n\nimport", "\nimport")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
