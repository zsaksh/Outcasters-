package com.example.inference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlin.random.Random

import com.example.backend.models.ModelState
import com.example.backend.models.ModelParams

data class Diagnostics(
    val tokensPerSecond: Float = 0f,
    val memoryUsageMb: Int = 0
)

class LlamaInferenceEngine {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotInstalled)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()
    
    private val _diagnostics = MutableStateFlow(Diagnostics())
    val diagnostics: StateFlow<Diagnostics> = _diagnostics.asStateFlow()

    var threadCount = 4
    var contextWindow = 2048

    fun loadModel(modelPath: String) {
        unloadModel()
        System.gc()
        
        _modelState.value = ModelState.Active(
            modelName = modelPath.substringAfterLast("/"),
            params = ModelParams(contextWindow = contextWindow, threadCount = threadCount)
        )
        _diagnostics.value = Diagnostics(0f, Random.nextInt(800, 2000))
    }
    
    fun unloadModel() {
        _modelState.value = ModelState.NotInstalled
        _diagnostics.value = Diagnostics(0f, 0)
        System.gc()
    }
    
    fun generate(prompt: String): Flow<String> = flow {
        if (_modelState.value !is ModelState.Active) throw IllegalStateException("Model not loaded")
        
        val modeConcept = prompt.contains("Mode: Concept Learning")
        val modeLanguage = prompt.contains("Mode: Language Learning")
        val modeInterview = prompt.contains("Mode: Interview Prep")
        val modeScan = prompt.contains("Mode: Scan & Solve")
        val modeQuiz = prompt.contains("Mode: Quiz")
        
        val lowerPrompt = prompt.lowercase()
        val isCalculus = lowerPrompt.contains("calculus") || lowerPrompt.contains("derivative") || lowerPrompt.contains("integral")
        val isPhotosynthesis = lowerPrompt.contains("photosynthesis") || lowerPrompt.contains("plant")
        val isFz = lowerPrompt.contains("fz") || lowerPrompt.contains("analytic")
        
        val mockResponse = when {
            isCalculus -> "**Direct Answer**\nCalculus is the mathematical study of continuous change.\n\n**Explanation**\nIt has two major branches, differential calculus and integral calculus.\n\n**Example**\nFinding the speed of a falling object at a specific moment in time.\n\n**Key Idea**\nCalculus allows us to model and analyze systems that are constantly changing.\n\n**Short Summary**\nCalculus is the mathematics of change and motion."
            isPhotosynthesis -> "**Direct Answer**\nPhotosynthesis is the process by which plants use sunlight to create energy.\n\n**Explanation**\nThis process happens in the chloroplasts.\n\n**Example**\nA sunflower turning towards the sun.\n\n**Key Idea**\nLight energy is converted into chemical energy.\n\n**Short Summary**\nPlants make their own food using sunlight."
            isFz -> "**Direct Answer**\nAn analytic function is a function that is locally given by a convergent power series.\n\n**Explanation**\nIf a complex function f(z) is differentiable at every point in an open set, it is analytic.\n\n**Example**\nf(z) = z^2 is an analytic function on the entire complex plane.\n\n**Key Idea**\nAnalytic functions are smooth and preserve angles locally.\n\n**Short Summary**\nf(z) is analytic if it has a complex derivative everywhere."
            modeConcept -> "**Direct Answer**\nHere is a conceptual explanation of your topic.\n\n**Explanation**\nThis concept is fundamentally about understanding the core mechanisms.\n\n**Example**\nConsider a real-world scenario where this is applied.\n\n**Key Idea**\nUnderstanding the underlying structure simplifies complex problems.\n\n**Short Summary**\nA foundational concept that builds deeper knowledge."
            modeLanguage -> "**Translation**\nVoici la traduction.\n\n**Explanation**\nThis explains the grammar and usage of the translated phrase in context.\n\n**Grammar Note**\nNotice how the adjectives follow the noun in this specific language structure.\n\n**Example Sentence**\nCeci est un exemple de phrase. (This is an example sentence.)\n\n**Practice Prompt**\nTry forming a sentence using these new words!"
            modeInterview -> "**Short Answer**\nI approached the problem systematically to achieve a 40% improvement.\n\n**Strong Version**\nIn my previous role, I identified a bottleneck, led the refactoring of our data pipeline, and successfully reduced latency by 40%.\n\n**Follow-up Tip**\nBe ready to discuss the specific techniques and metrics you used to measure success."
            modeScan -> "**Step 1: Identify the problem**\nWe need to analyze the provided text or equation.\n\n**Step 2: Apply the rule**\nUsing standard principles, we break down the problem into solvable parts.\n\n**Step 3: Solution**\nThe final result is derived from the steps above.\n\n**Concept**\nUnderstanding the core principle ensures you can solve similar problems."
            modeQuiz -> "Here's a quick quiz for you:\n\nWhat is the main function of the mitochondria in a cell?\n\nA) Photosynthesis\nB) Cellular Respiration\nC) Protein synthesis\n\nTake a guess!"
            else -> "I can certainly help you with that. Let's break it down step by step."
        }
        
        // Simple tokenization for typing effect
        var currentToken = ""
        for (char in mockResponse) {
            currentToken += char
            if (char == ' ' || char == '\n') {
                emit(currentToken)
                currentToken = ""
                _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = Random.nextFloat() * 15 + 10)
                delay(20)
            }
        }
        if (currentToken.isNotEmpty()) {
            emit(currentToken)
        }
        _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
    }
    
    fun stopGeneration() {
        if (_modelState.value is ModelState.Active) {
            _diagnostics.value = _diagnostics.value.copy(tokensPerSecond = 0f)
        }
    }
}
