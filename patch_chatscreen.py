import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

language_selector = """
            if (currentMode == "language" || currentMode == "translate" || currentMode == "vocabulary" || currentMode == "grammar" || currentMode == "practice") {
                val languages = listOf("French", "Spanish", "Japanese", "German", "Mandarin")
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language Mode:", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(currentTargetLanguage)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        currentTargetLanguage = lang
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Tasks row
                val langTasks = listOf("Translate", "Vocabulary", "Grammar", "Practice")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    langTasks.forEach { task ->
                        val isSelected = currentMode == task.lowercase() || (currentMode == "language" && task == "Practice")
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentMode = task.lowercase() },
                            label = { Text(task) }
                        )
                    }
                }
                HorizontalDivider()
            }
"""

content = content.replace(
    "    ) { paddingValues ->\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(paddingValues)\n        ) {",
    "    ) { paddingValues ->\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(paddingValues)\n        ) {\n" + language_selector
)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
