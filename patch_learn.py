import re

with open("app/src/main/java/com/example/ui/screens/LearnScreen.kt", "r") as f:
    content = f.read()

content = content.replace('var selectedMode by remember { mutableStateOf("Concept") }', 'var selectedMode by remember { mutableStateOf("Language") }')
content = content.replace('val modes = listOf("Concept", "Language", "Interview")', 'val modes = listOf("Language", "Interview")')
content = content.replace('"Concept" -> ConceptGrid(navController)\n', '')

content = re.sub(r'@Composable\nfun ConceptGrid\(navController: NavController\) \{.*?\n\}\n\n', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/LearnScreen.kt", "w") as f:
    f.write(content)
