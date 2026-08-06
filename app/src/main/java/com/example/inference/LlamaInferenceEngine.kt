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
        
        val mockResponse = when {
            isCalculus -> "**Direct Answer**\nCalculus is the mathematical study of continuous change, in the same way that geometry is the study of shape, and algebra is the study of generalizations of arithmetic operations.\n\n**Explanation**\nIt has two major branches, differential calculus and integral calculus. Differential calculus concerns instantaneous rates of change, and the slopes of curves, while integral calculus concerns accumulation of quantities, and areas under or between curves.\n\n**Example**\nFinding the speed of a falling object at a specific moment in time (differential calculus).\n\n**Key Idea**\nCalculus allows us to model and analyze systems that are constantly changing.\n\n**Short Summary**\nCalculus is the mathematics of change and motion."
            isPhotosynthesis || modeConcept -> "**Direct Answer**\nPhotosynthesis is the process by which plants use sunlight, water, and carbon dioxide to create oxygen and energy in the form of sugar.\n\n**Explanation**\nThis process happens in the chloroplasts. The chlorophyll absorbs light, which provides the energy to drive the chemical reactions.\n\n**Example**\nA sunflower turning towards the sun to gather light for energy production.\n\n**Key Idea**\nLight energy is converted into chemical energy.\n\n**Short Summary**\nPlants make their own food using sunlight."
            modeLanguage -> "**Translation**\nMerci beaucoup.\n\n**Explanation**\n'Merci' means thank you, and 'beaucoup' means very much. Used together it means thank you very much.\n\n**Grammar Note**\nIn French, adjectives like 'beaucoup' follow the verb they modify, but here it acts as an adverb intensifying 'merci'.\n\n**Example Sentence**\nMerci beaucoup pour votre aide. (Thank you very much for your help.)\n\n**Practice Prompt**\nTry saying 'Thank you for the gift' in French!"
            modeInterview -> "**Short Answer**\nI implemented a lazy loading strategy which reduced initial load time by 40%.\n\n**Strong Version**\nIn my previous role, our application suffered from 5-second load times. I led the refactoring of our data pipeline to use a lazy-loading architecture with pagination. This reduced the initial load time to 1.2 seconds (a 76% improvement) and increased user retention by 15%.\n\n**Follow-up Tip**\nBe ready to discuss the specific pagination technique you used (e.g., offset vs. cursor-based)."
            modeScan -> "**Step 1: Identify the problem**\nWe need to find the derivative of f(x) = sin(x).\n\n**Step 2: Apply the rule**\nThe derivative of sin(x) with respect to x is a standard trigonometric derivative.\n\n**Step 3: Solution**\nf'(x) = cos(x).\n\n**Concept**\nDerivatives represent the rate of change. The rate of change of a sine wave is represented by a cosine wave."
            modeQuiz -> "Here's a quick quiz for you:\n\nWhat is the main function of the mitochondria in a cell?\n\nA) Photosynthesis\nB) Cellular Respiration (Energy production)\nC) Protein synthesis\n\nTake a guess!"
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
