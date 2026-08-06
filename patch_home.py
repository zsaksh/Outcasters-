import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

replacement = """
    val recentSessions by app.container.chatDao.getAllSessions().collectAsState(initial = emptyList())

    Scaffold(
"""

content = content.replace("    Scaffold(\n", replacement)

replacement2 = """
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recentSessions.isEmpty()) {
                        Text("No recent activity", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        recentSessions.take(3).forEach { session ->
                            RecentActivityItem(
                                title = session.title,
                                time = android.text.format.DateUtils.getRelativeTimeSpanString(session.timestamp).toString(),
                                onClick = { navController.navigate("chat/${session.id}") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
"""

content = re.sub(r'            item \{\n                Column\(verticalArrangement = Arrangement.spacedBy\(8.dp\)\) \{\n                    RecentActivityItem\(title = "Derivative of sin\(x\)", time = "2m ago"\)\n                    RecentActivityItem\(title = "Photosynthesis Process", time = "1h ago"\)\n                    RecentActivityItem\(title = "French Vocabulary", time = "Yesterday"\)\n                \}\n                Spacer\(modifier = Modifier.height\(16.dp\)\)\n            \}', replacement2, content)

content = content.replace("fun RecentActivityItem(title: String, time: String) {", "fun RecentActivityItem(title: String, time: String, onClick: () -> Unit = {}) {")
content = content.replace(".clickable { }", ".clickable { onClick() }")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
