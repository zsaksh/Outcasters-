import re

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "r") as f:
    content = f.read()

replacement = """        val modeConcept = prompt.contains("Mode: Concept Learning")
        val modeLanguage = prompt.contains("Mode: Language Learning")
        val modeInterview = prompt.contains("Mode: Interview Prep")
        val modeScan = prompt.contains("Mode: Scan & Solve")
        val modeQuiz = prompt.contains("Mode: Quiz")
        
        val lowerPrompt = prompt.lowercase()
        val isCalculus = lowerPrompt.contains("calculus") || lowerPrompt.contains("derivative") || lowerPrompt.contains("integral")
        val isPhotosynthesis = lowerPrompt.contains("photosynthesis") || lowerPrompt.contains("plant")
        val isFz = lowerPrompt.contains("fz") || lowerPrompt.contains("analytic")
        
        val mockResponse = when {
            isCalculus -> "**Direct Answer**\\nCalculus is the mathematical study of continuous change, in the same way that geometry is the study of shape, and algebra is the study of generalizations of arithmetic operations.\\n\\n**Explanation**\\nIt has two major branches, differential calculus and integral calculus. Differential calculus concerns instantaneous rates of change, and the slopes of curves, while integral calculus concerns accumulation of quantities, and areas under or between curves.\\n\\n**Example**\\nFinding the speed of a falling object at a specific moment in time (differential calculus).\\n\\n**Key Idea**\\nCalculus allows us to model and analyze systems that are constantly changing.\\n\\n**Short Summary**\\nCalculus is the mathematics of change and motion."
            isPhotosynthesis -> "**Direct Answer**\\nPhotosynthesis is the process by which plants use sunlight, water, and carbon dioxide to create oxygen and energy in the form of sugar.\\n\\n**Explanation**\\nThis process happens in the chloroplasts. The chlorophyll absorbs light, which provides the energy to drive the chemical reactions.\\n\\n**Example**\\nA sunflower turning towards the sun to gather light for energy production.\\n\\n**Key Idea**\\nLight energy is converted into chemical energy.\\n\\n**Short Summary**\\nPlants make their own food using sunlight."
            isFz -> "**Direct Answer**\\nAn analytic function is a function that is locally given by a convergent power series. In complex analysis, it refers to a function that is complex differentiable at every point of its domain.\\n\\n**Explanation**\\nIf a complex function f(z) is differentiable at every point in an open set, it is said to be analytic (or holomorphic). This implies it is infinitely differentiable and can be represented by its Taylor series.\\n\\n**Example**\\nf(z) = z^2 is an analytic function on the entire complex plane.\\n\\n**Key Idea**\\nAnalytic functions are smooth and preserve angles locally.\\n\\n**Short Summary**\\nf(z) is analytic if it has a complex derivative everywhere in its domain."
            modeConcept -> "**Direct Answer**\\nHere is a conceptual explanation of your topic.\\n\\n**Explanation**\\nThis concept is fundamentally about understanding the core mechanisms and relationships of the subject.\\n\\n**Example**\\nConsider a real-world scenario where this principle is applied directly.\\n\\n**Key Idea**\\nUnderstanding the underlying structure simplifies complex problems.\\n\\n**Short Summary**\\nA foundational concept that builds deeper knowledge."
            modeLanguage -> "**Translation**\\nVoici la traduction.\\n\\n**Explanation**\\nThis explains the grammar and usage of the translated phrase in context.\\n\\n**Grammar Note**\\nNotice how the adjectives follow the noun in this specific language structure.\\n\\n**Example Sentence**\\nCeci est un exemple de phrase. (This is an example sentence.)\\n\\n**Practice Prompt**\\nTry forming a sentence using these new words!"
            modeInterview -> "**Short Answer**\\nI approached the problem systematically to achieve a 40% improvement.\\n\\n**Strong Version**\\nIn my previous role, I identified a bottleneck, led the refactoring of our data pipeline, and successfully reduced latency by 40%, which increased user retention by 15%.\\n\\n**Follow-up Tip**\\nBe ready to discuss the specific techniques and metrics you used to measure success."
            modeScan -> "**Step 1: Identify the problem**\\nWe need to analyze the provided text or equation.\\n\\n**Step 2: Apply the rule**\\nUsing standard principles, we break down the problem into solvable parts.\\n\\n**Step 3: Solution**\\nThe final result is derived from the steps above.\\n\\n**Concept**\\nUnderstanding the core principle ensures you can solve similar problems."
            modeQuiz -> "Here's a quick quiz for you:\\n\\nWhat is the main function of the mitochondria in a cell?\\n\\nA) Photosynthesis\\nB) Cellular Respiration (Energy production)\\nC) Protein synthesis\\n\\nTake a guess!"
            else -> "I can certainly help you with that. Let's break it down step by step."
        }"""

content = re.sub(r'        val modeConcept = prompt.contains\("Mode: Concept Learning"\).*?        \}', replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/inference/LlamaInferenceEngine.kt", "w") as f:
    f.write(content)
