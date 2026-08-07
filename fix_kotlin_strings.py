import re

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    text = f.read()

new_mock = '''        val mockResponse = when {
            isCalculus -> """**Direct Answer**
Calculus is the mathematical study of continuous change.

**Explanation**
It has two major branches, differential calculus and integral calculus.

**Example**
Finding the speed of a falling object at a specific moment in time.

**Key Idea**
Calculus allows us to model and analyze systems that are constantly changing.

**Short Summary**
Calculus is the mathematics of change and motion."""
            isPhotosynthesis -> """**Direct Answer**
Photosynthesis is the process by which plants use sunlight to create energy.

**Explanation**
This process happens in the chloroplasts.

**Example**
A sunflower turning towards the sun.

**Key Idea**
Light energy is converted into chemical energy.

**Short Summary**
Plants make their own food using sunlight."""
            isAi -> """**Direct Answer**
Artificial Intelligence (AI) is the simulation of human intelligence in machines.

**Explanation**
It involves learning, reasoning, and self-correction.

**Example**
Virtual assistants like Siri and Alexa.

**Key Idea**
AI enables computers to perform tasks that typically require human intellect.

**Short Summary**
AI is machine intelligence."""
            isTranslate -> """**Translation**
Bonjour

**Explanation**
'Hello' translates directly to 'Bonjour' in French.

**Grammar Note**
It is used as a formal greeting during the day.

**Example Sentence**
Bonjour, comment allez-vous?

**Practice Prompt**
Try saying hello in a full sentence."""
            isGravity -> """**Direct Answer**
Gravity is the force that attracts a body towards the center of the earth, or towards any other physical body having mass.

**Explanation**
It is what gives weight to physical objects.

**Example**
An apple falling from a tree.

**Key Idea**
Mass attracts mass.

**Short Summary**
Gravity is the force of attraction between masses."""
            isQuizMe -> """Here's a quick quiz for you:

What is the main function of the mitochondria in a cell?

A) Photosynthesis
B) Cellular Respiration
C) Protein synthesis

Take a guess!"""
            isSummarize -> """Here is a summary of your text:

- It covers the main point.
- It highlights key details.
- It concludes with a final thought."""
            isCompare -> """**Comparison: Java vs Kotlin**

**Java**
- Older, more established
- Verbose syntax
- Checked exceptions

**Kotlin**
- Modern, concise syntax
- Null safety built-in
- Coroutines for async programming

**Key Difference**
Kotlin reduces boilerplate and improves safety compared to Java."""
            isFz -> """**Direct Answer**
An analytic function is a function that is locally given by a convergent power series.

**Explanation**
If a complex function f(z) is differentiable at every point in an open set, it is analytic.

**Example**
f(z) = z^2 is an analytic function on the entire complex plane.

**Key Idea**
Analytic functions are smooth and preserve angles locally.

**Short Summary**
f(z) is analytic if it has a complex derivative everywhere."""
            modeConcept -> """**Direct Answer**
Here is a conceptual explanation of your topic.

**Explanation**
This concept is fundamentally about understanding the core mechanisms.

**Example**
Consider a real-world scenario where this is applied.

**Key Idea**
Understanding the underlying structure simplifies complex problems.

**Short Summary**
A foundational concept that builds deeper knowledge."""
            modeLanguage -> """**Translation**
Voici la traduction.

**Explanation**
This explains the grammar and usage of the translated phrase in context.

**Grammar Note**
Notice how the adjectives follow the noun in this specific language structure.

**Example Sentence**
Ceci est un exemple de phrase. (This is an example sentence.)

**Practice Prompt**
Try forming a sentence using these new words!"""
            modeInterview -> """**Short Answer**
I approached the problem systematically to achieve a 40% improvement.

**Strong Version**
In my previous role, I identified a bottleneck, led the refactoring of our data pipeline, and successfully reduced latency by 40%.

**Follow-up Tip**
Be ready to discuss the specific techniques and metrics you used to measure success."""
            modeScan -> """**Step 1: Identify the problem**
We need to analyze the provided text or equation.

**Step 2: Apply the rule**
Using standard principles, we break down the problem into solvable parts.

**Step 3: Solution**
The final result is derived from the steps above.

**Concept**
Understanding the core principle ensures you can solve similar problems."""
            modeQuiz -> """Here's a quick quiz for you:

What is the main function of the mitochondria in a cell?

A) Photosynthesis
B) Cellular Respiration
C) Protein synthesis

Take a guess!"""
            else -> """Based on your request regarding "$newTaskLower", I can help you with that. The key concept here is understanding the core mechanisms of your query. 

Here is a brief summary of what you need to know, broken down step by step. If you have any more specific questions, feel free to ask!"""
        }'''

pattern = r'val mockResponse = when \{.*?    \}'
text = re.sub(pattern, new_mock, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "r") as f:
    text2 = f.read()

new_func = '''    private fun getSystemContext(mode: String, targetLanguage: String): String {
        val basePrompt = """You are Outcasters AI, a private local study assistant. Answer clearly, directly, and helpfully. Never mention internal runtime details, threads, context size, token counts, logs, or system internals. Adapt your answer to the selected mode. Keep the response natural, correct, and concise unless the user asks for more detail."""
        
        return basePrompt + "\n\n" + when (mode.lowercase()) {
            "concept", "teacher" -> """Mode: Concept Learning.
You are a helpful academic tutor. Answer clearly and directly. Explain simply, give step-by-step reasoning, provide examples, summarize key points, and avoid unnecessary jargon.
Output structure:
- Direct Answer
- Explanation
- Example
- Key Idea
- Short Summary"""
            "math" -> """Mode: Math/Calculus.
You are a math tutor. Answer only the current math or calculus question. Do not add unrelated content. If the query is ambiguous, ask for clarification. For exact math problems, solve step by step.
Output structure:
- Direct Answer
- Step-by-step solution
- Final result
- Short summary"""
            "translate" -> """Mode: Translate.
Translate the user's input strictly into $targetLanguage. Do not add conversational filler. Only provide the translation."""
            "grammar" -> """Mode: Grammar Correction.
Identify grammatical errors in the user's input and provide the corrected version along with a brief explanation of the rules violated."""
            "vocabulary" -> """Mode: Vocabulary.
Explain the definition, origin, and usage of the provided word. Give 3 example sentences in different contexts."""
            "practice" -> """Mode: Language Practice.
Act as a conversational partner in $targetLanguage. Reply naturally, and gently correct any major mistakes the user makes."""
            "language" -> """Mode: Language Learning ($targetLanguage).
You are a language tutor. Teach in the selected language. Respond in the selected target language when appropriate. Explain grammar clearly, give translations, provide short examples, and generate practice exercises if requested.
Output structure:
- Translation
- Explanation
- Grammar Note
- Example Sentence
- Practice Prompt"""
            "interview" -> """Mode: Interview Prep.
Give concise, structured answers. Use a professional tone, provide feedback, and avoid overly long explanations unless asked.
Output structure:
- Short Answer
- Strong Version
- Follow-up Tip"""
            "scan_solve" -> """Mode: Scan & Solve.
Use OCR text only if it matches the question. Clean the OCR text first. Do not answer from stale OCR from earlier scans. Solve the question directly. Show step-by-step logic, explain formulas or concepts, and avoid meta commentary."""
            "quiz" -> """Mode: Quiz.
Generate a short quiz based on the user's topic. Wait for their answer, then provide constructive feedback and the correct answer."""
            "summarize" -> """Mode: Summarize.
Provide a concise, bulleted summary of the core points from the user's input. Strip away fluff."""
            "compare" -> """Mode: Compare.
Create a structured comparison of the entities provided. Use bullet points or a simulated table format highlighting pros, cons, and key differences."""
            "step_by_step" -> """Mode: Step-by-Step.
Break down the solution or explanation into numbered, logical steps. Make each step actionable and clear."""
            "explain_simply" -> """Mode: Explain Simply.
Explain the concept as if the user is a beginner. Use analogies, simple words, and avoid all technical jargon."""
            else -> """Mode: General Chat.
Be conversational but stay relevant. Do not drift into unrelated topics."""
        }
    }'''

pattern2 = r'private fun getSystemContext.*?\}    \}'
text2 = re.sub(pattern2, new_func, text2, flags=re.DOTALL)

with open("app/src/main/java/com/example/backend/inference/PromptBuilder.kt", "w") as f:
    f.write(text2)

