import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

replacement = """
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filteredSessions = recentSessions.filter { 
                        it.title.isNotBlank() && !it.title.contains("Context from", ignoreCase = true) && !it.title.contains("Debug", ignoreCase = true)
                    }
                    if (filteredSessions.isEmpty()) {
                        Text("No recent activity", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        filteredSessions.take(3).forEach { session ->
                            RecentActivityItem(
"""

content = re.sub(r'                Column\(verticalArrangement = Arrangement.spacedBy\(8.dp\)\) \{\n                    if \(recentSessions.isEmpty\(\)\) \{\n                        Text\("No recent activity", color = TextSecondary, fontSize = 14.sp\)\n                    \} else \{\n                        recentSessions.take\(3\).forEach \{ session ->\n                            RecentActivityItem\(', replacement, content)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
