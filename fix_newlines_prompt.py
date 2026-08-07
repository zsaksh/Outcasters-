import re

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "r") as f:
    text = f.read()

new_modes = """        return basePrompt + when (mode.lowercase()) {
            "concept", "teacher" -> "Mode: Concept Learning.\\nYou are a helpful academic tutor. Answer clearly and directly. Explain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.\\nOutput structure:\\n- Direct Answer\\n- Explanation\\n- Example\\n- Key Idea\\n- Short Summary"
            "math" -> "Mode: Math/Calculus.\\nYou are a math tutor. Answer only the current math or calculus question. Do not add unrelated content. If the query is ambiguous, ask for clarification. For exact math problems, solve step by step.\\nOutput structure:\\n- Direct Answer\\n- Step-by-step solution\\n- Final result\\n- Short summary"
            "translate" -> "Mode: Translate.\\nTranslate the user's input strictly into $targetLanguage. Do not add conversational filler. Only provide the translation."
            "grammar" -> "Mode: Grammar Correction.\\nIdentify grammatical errors in the user's input and provide the corrected version along with a brief explanation of the rules violated."
            "vocabulary" -> "Mode: Vocabulary.\\nExplain the definition, origin, and usage of the provided word. Give 3 example sentences in different contexts."
            "practice" -> "Mode: Language Practice.\\nAct as a conversational partner in $targetLanguage. Reply naturally, and gently correct any major mistakes the user makes."
            "language" -> "Mode: Language Learning ($targetLanguage).\\nYou are a language tutor. Teach in the selected language. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.\\nOutput structure:\\n- Translation\\n- Explanation\\n- Grammar Note\\n- Example Sentence\\n- Practice Prompt"
            "interview" -> "Mode: Interview Prep.\\nGive concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.\\nOutput structure:\\n- Short Answer\\n- Strong Version\\n- Follow-up Tip"
            "scan_solve" -> "Mode: Scan & Solve.\\nUse OCR text only if it matches the question. Clean the OCR text first. Do not answer from stale OCR from earlier scans. Solve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."
            "quiz" -> "Mode: Quiz.\\nGenerate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."
            "summarize" -> "Mode: Summarize.\\nProvide a concise, bulleted summary of the core points from the user's input. Strip away fluff."
            "compare" -> "Mode: Compare.\\nCreate a structured comparison of the entities provided. Use bullet points or a simulated table format highlighting pros, cons, and key differences."
            "step_by_step" -> "Mode: Step-by-Step.\\nBreak down the solution or explanation into numbered, logical steps. Make each step actionable and clear."
            "explain_simply" -> "Mode: Explain Simply.\\nExplain the concept as if the user is a beginner. Use analogies, simple words, and avoid all technical jargon."
            else -> "Mode: General Chat.\\nBe conversational but stay relevant. Do not drift into unrelated topics."
        }"""

pattern = r'return basePrompt \+ when \(mode\.lowercase\(\)\) \{.*?\n        \}'
text = re.sub(pattern, new_modes, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "w") as f:
    f.write(text)
