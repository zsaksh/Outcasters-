import re

with open("app/src/main/java/com/example/ui/screens/LearnScreen.kt", "r") as f:
    content = f.read()

content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Explain Simply", tint = Color(0xFFFFB74D))', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Explain Simply", tint = Color(0xFFFFB74D), route = "chat?mode=concept")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Step by Step", tint = AccentPurple)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Step by Step", tint = AccentPurple, route = "chat?mode=concept")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Examples", tint = Color(0xFFAED581))', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Examples", tint = Color(0xFFAED581), route = "chat?mode=concept")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.CompareArrows, title = "Compare Topics", tint = AccentOrange)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.CompareArrows, title = "Compare Topics", tint = AccentOrange, route = "chat?mode=concept")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.QuestionMark, title = "Quiz Me", tint = Color(0xFFF06292))', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.QuestionMark, title = "Quiz Me", tint = Color(0xFFF06292), route = "chat?mode=quiz")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Summarize, title = "Summarize", tint = AccentTeal)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Summarize, title = "Summarize", tint = AccentTeal, route = "chat?mode=concept")')

content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Translate", tint = AccentTeal)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Translate", tint = AccentTeal, route = "chat?mode=translate")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Vocabulary", tint = AccentPurple)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Vocabulary", tint = AccentPurple, route = "chat?mode=vocabulary")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Grammar", tint = AccentOrange)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Grammar", tint = AccentOrange, route = "chat?mode=grammar")')

content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "System Design", tint = AccentOrange)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "System Design", tint = AccentOrange, route = "chat?mode=interview")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Case Math", tint = Color(0xFFAED581))', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Case Math", tint = Color(0xFFAED581), route = "chat?mode=interview")')
content = content.replace('LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "STAR Refiner", tint = AccentPurple)', 'LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "STAR Refiner", tint = AccentPurple, route = "chat?mode=interview")')

with open("app/src/main/java/com/example/ui/screens/LearnScreen.kt", "w") as f:
    f.write(content)
