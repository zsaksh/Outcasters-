package com.example.backend.inference

class PostProcessor {
    fun cleanResponse(text: String): String {
        var clean = text
        // Remove common technical debug leaks
        val debugPatterns = listOf(
            Regex("""\(Threads: \d+, Ctx: \d+\)""", RegexOption.IGNORE_CASE),
            Regex("""\[Model: .*?\]""", RegexOption.IGNORE_CASE),
            Regex("""\[Context size: \d+\]""", RegexOption.IGNORE_CASE),
            Regex("""Local inference response.*?:""", RegexOption.IGNORE_CASE),
            Regex("""<\|.*?\|>""") // Any stray tags
        )
        
        for (pattern in debugPatterns) {
            clean = clean.replace(pattern, "")
        }
        return clean.trim()
    }
}
