import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Remove the appended helper code at the end of the file if it exists
helper_start = "import androidx.compose.ui.text.AnnotatedString"
if helper_start in content:
    parts = content.split(helper_start)
    if len(parts) > 1:
        # We know it was appended, let's remove everything after and including helper_start
        content = parts[0]

# Add imports at the top
imports = """
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
"""

content = content.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\n" + imports)

# Add the function near the top (e.g. before @Composable fun ChatScreen)
func = """
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        var isBold = false
        for (part in parts) {
            if (isBold) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
            isBold = !isBold
        }
    }
}
"""

content = content.replace("@Composable\nfun ChatScreen", func + "\n@Composable\nfun ChatScreen")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
