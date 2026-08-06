import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Add a markdown parsing helper at the top or bottom of the file
helper_code = """
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

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
if "fun parseMarkdown(" not in content:
    content = content + "\n" + helper_code

# Now replace where messages are displayed
# We have a Text for message.content
# Let's replace `Text(text = message.content,` with `Text(text = parseMarkdown(message.content),`
# and `Text(text = streamingResponse,` with `Text(text = parseMarkdown(streamingResponse),`
content = content.replace("Text(text = message.content,", "Text(text = parseMarkdown(message.content),")
content = content.replace("Text(text = streamingResponse,", "Text(text = parseMarkdown(streamingResponse),")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
