import re

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    text = f.read()

new_mock = r'''        val mockResponse = when {
            isCalculus -> "**Direct Answer**\nCalculus is the mathematical study of continuous change.\n\n**Explanation**\nIt has two major branches, differential calculus and integral calculus.\n\n**Example**\nFinding the speed of a falling object at a specific moment in time.\n\n**Key Idea**\nCalculus allows us to model and analyze systems that are constantly changing.\n\n**Short Summary**\nCalculus is the mathematics of change and motion."
            isPhotosynthesis -> "**Direct Answer**\nPhotosynthesis is the process by which plants use sunlight to create energy.\n\n**Explanation**\nThis process happens in the chloroplasts.\n\n**Example**\nA sunflower turning towards the sun.\n\n**Key Idea**\nLight energy is converted into chemical energy.\n\n**Short Summary**\nPlants make their own food using sunlight."
            isAi -> "**Direct Answer**\nArtificial Intelligence (AI) is the simulation of human intelligence in machines.\n\n**Explanation**\nIt involves learning, reasoning, and self-correction.\n\n**Example**\nVirtual assistants like Siri and Alexa.\n\n**Key Idea**\nAI enables computers to perform tasks that typically require human intellect.\n\n**Short Summary**\nAI is machine intelligence."
            isTranslate -> "**Translation**\nBonjour\n\n**Explanation**\n'Hello' translates directly to 'Bonjour' in French.\n\n**Grammar Note**\nIt is used as a formal greeting during the day.\n\n**Example Sentence**\nBonjour, comment allez-vous?\n\n**Practice Prompt**\nTry saying hello in a full sentence."
            isGravity -> "**Direct Answer**\nGravity is the force that attracts a body towards the center of the earth, or towards any other physical body having mass.\n\n**Explanation**\nIt is what gives weight to physical objects.\n\n**Example**\nAn apple falling from a tree.\n\n**Key Idea**\nMass attracts mass.\n\n**Short Summary**\nGravity is the force of attraction between masses."
            isQuizMe -> "Here's a quick quiz for you:\n\nWhat is the main function of the mitochondria in a cell?\n\nA) Photosynthesis\nB) Cellular Respiration\nC) Protein synthesis\n\nTake a guess!"
            isSummarize -> "Here is a summary of your text:\n\n- It covers the main point.\n- It highlights key details.\n- It concludes with a final thought."
            isCompare -> "**Comparison: Java vs Kotlin**\n\n**Java**\n- Older, more established\n- Verbose syntax\n- Checked exceptions\n\n**Kotlin**\n- Modern, concise syntax\n- Null safety built-in\n- Coroutines for async programming\n\n**Key Difference**\nKotlin reduces boilerplate and improves safety compared to Java."
            isFz -> "**Direct Answer**\nAn analytic function is a function that is locally given by a convergent power series.\n\n**Explanation**\nIf a complex function f(z) is differentiable at every point in an open set, it is analytic.\n\n**Example**\nf(z) = z^2 is an analytic function on the entire complex plane.\n\n**Key Idea**\nAnalytic functions are smooth and preserve angles locally.\n\n**Short Summary**\nf(z) is analytic if it has a complex derivative everywhere."
            modeConcept -> "**Direct Answer**\nHere is a conceptual explanation of your topic.\n\n**Explanation**\nThis concept is fundamentally about understanding the core mechanisms.\n\n**Example**\nConsider a real-world scenario where this is applied.\n\n**Key Idea**\nUnderstanding the underlying structure simplifies complex problems.\n\n**Short Summary**\nA foundational concept that builds deeper knowledge."
            modeLanguage -> "**Translation**\nVoici la traduction.\n\n**Explanation**\nThis explains the grammar and usage of the translated phrase in context.\n\n**Grammar Note**\nNotice how the adjectives follow the noun in this specific language structure.\n\n**Example Sentence**\nCeci est un exemple de phrase. (This is an example sentence.)\n\n**Practice Prompt**\nTry forming a sentence using these new words!"
            modeInterview -> "**Short Answer**\nI approached the problem systematically to achieve a 40% improvement.\n\n**Strong Version**\nIn my previous role, I identified a bottleneck, led the refactoring of our data pipeline, and successfully reduced latency by 40%.\n\n**Follow-up Tip**\nBe ready to discuss the specific techniques and metrics you used to measure success."
            modeScan -> "**Step 1: Identify the problem**\nWe need to analyze the provided text or equation.\n\n**Step 2: Apply the rule**\nUsing standard principles, we break down the problem into solvable parts.\n\n**Step 3: Solution**\nThe final result is derived from the steps above.\n\n**Concept**\nUnderstanding the core principle ensures you can solve similar problems."
            modeQuiz -> "Here's a quick quiz for you:\n\nWhat is the main function of the mitochondria in a cell?\n\nA) Photosynthesis\nB) Cellular Respiration\nC) Protein synthesis\n\nTake a guess!"
            else -> "Based on your request regarding \"$newTaskLower\", I can help you with that. The key concept here is understanding the core mechanisms of your query. \n\nHere is a brief summary of what you need to know, broken down step by step. If you have any more specific questions, feel free to ask!"
        }'''

pattern = r'val mockResponse = when \{.*?    \}'
text = re.sub(pattern, new_mock, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "r") as f:
    text2 = f.read()

new_func = r'''    private fun getSystemContext(mode: String, targetLanguage: String): String {
        val basePrompt = "You are Outcasters AI, a private local study assistant. Answer clearly, directly, and helpfully. Never mention internal runtime details, threads, context size, token counts, logs, or system internals. Adapt your answer to the selected mode. Keep the response natural, correct, and concise unless the user asks for more detail.\n\n"
        
        return basePrompt + when (mode.lowercase()) {
            "concept", "teacher" -> "Mode: Concept Learning.\nYou are a helpful academic tutor. Answer clearly and directly. Explain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.\nOutput structure:\n- Direct Answer\n- Explanation\n- Example\n- Key Idea\n- Short Summary"
            "math" -> "Mode: Math/Calculus.\nYou are a math tutor. Answer only the current math or calculus question. Do not add unrelated content. If the query is ambiguous, ask for clarification. For exact math problems, solve step by step.\nOutput structure:\n- Direct Answer\n- Step-by-step solution\n- Final result\n- Short summary"
            "translate" -> "Mode: Translate.\nTranslate the user's input strictly into $targetLanguage. Do not add conversational filler. Only provide the translation."
            "grammar" -> "Mode: Grammar Correction.\nIdentify grammatical errors in the user's input and provide the corrected version along with a brief explanation of the rules violated."
            "vocabulary" -> "Mode: Vocabulary.\nExplain the definition, origin, and usage of the provided word. Give 3 example sentences in different contexts."
            "practice" -> "Mode: Language Practice.\nAct as a conversational partner in $targetLanguage. Reply naturally, and gently correct any major mistakes the user makes."
            "language" -> "Mode: Language Learning ($targetLanguage).\nYou are a language tutor. Teach in the selected language. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.\nOutput structure:\n- Translation\n- Explanation\n- Grammar Note\n- Example Sentence\n- Practice Prompt"
            "interview" -> "Mode: Interview Prep.\nGive concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.\nOutput structure:\n- Short Answer\n- Strong Version\n- Follow-up Tip"
            "scan_solve" -> "Mode: Scan & Solve.\nUse OCR text only if it matches the question. Clean the OCR text first. Do not answer from stale OCR from earlier scans. Solve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."
            "quiz" -> "Mode: Quiz.\nGenerate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."
            "summarize" -> "Mode: Summarize.\nProvide a concise, bulleted summary of the core points from the user's input. Strip away fluff."
            "compare" -> "Mode: Compare.\nCreate a structured comparison of the entities provided. Use bullet points or a simulated table format highlighting pros, cons, and key differences."
            "step_by_step" -> "Mode: Step-by-Step.\nBreak down the solution or explanation into numbered, logical steps. Make each step actionable and clear."
            "explain_simply" -> "Mode: Explain Simply.\nExplain the concept as if the user is a beginner. Use analogies, simple words, and avoid all technical jargon."
            else -> "Mode: General Chat.\nBe conversational but stay relevant. Do not drift into unrelated topics."
        }
    }'''

pattern2 = r'private fun getSystemContext.*?\}    \}'
text2 = re.sub(pattern2, new_func, text2, flags=re.DOTALL)

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "w") as f:
    f.write(text2)

