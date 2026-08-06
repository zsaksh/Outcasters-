package com.example.backend.interview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class InterviewState {
    IDLE,
    INITIALIZING,
    INTERVIEWER_SPEAKING,
    WAITING_FOR_USER,
    USER_SPEAKING,
    ANALYZING_RESPONSE,
    CONCLUDING,
    FINISHED
}

data class InterviewPersona(
    val id: String,
    val name: String,
    val domain: String,
    val style: String
)

data class InterviewTurn(
    val speaker: String,
    val message: String
)

class InterviewEngine {
    private val _currentState = MutableStateFlow(InterviewState.IDLE)
    val currentState: StateFlow<InterviewState> = _currentState.asStateFlow()

    private val _transcript = MutableStateFlow<List<InterviewTurn>>(emptyList())
    val transcript: StateFlow<List<InterviewTurn>> = _transcript.asStateFlow()

    private var activePersona: InterviewPersona? = null
    private var turnCount = 0
    private val maxTurns = 5
    
    private val vadClient = SileroVadClient()
    private val scoringEngine = ScoringEngine()
    private var currentReport: InterviewReport? = null
    
    private var silenceFramesCount = 0
    private val silenceThresholdFrames = 30 // Approx 1 second depending on frame size

    fun initializeSession(targetRole: String, domain: String, difficulty: String) {
        _currentState.value = InterviewState.INITIALIZING
        
        // Initialize Mock VAD
        vadClient.initVadModelMock("assets/silero_vad.onnx")
        
        // Select Persona based on domain
        activePersona = when {
            domain.contains("Tech") -> InterviewPersona("1", "Alex", "Engineering", "Sharp, technical, probing")
            domain.contains("Consulting") -> InterviewPersona("2", "Morgan", "Consulting", "Data-driven, structured")
            domain.contains("Behavioral") -> InterviewPersona("3", "Jordan", "Leadership", "Empathetic, metric-focused")
            domain.contains("Finance") -> InterviewPersona("4", "Taylor", "Finance", "Fast-paced, rigorous")
            else -> InterviewPersona("5", "Casey", "HR", "Warm, behavioral")
        }
        
        turnCount = 0
        _transcript.value = emptyList()
        currentReport = null
        silenceFramesCount = 0
        
        startNextTurn("Welcome. To start, could you walk me through your experience relevant to $targetRole?")
    }

    fun startNextTurn(interviewerMessage: String) {
        _currentState.value = InterviewState.INTERVIEWER_SPEAKING
        _transcript.update { it + InterviewTurn("Interviewer", interviewerMessage) }
        
        // Simulate interviewer speaking completion
        _currentState.value = InterviewState.WAITING_FOR_USER
    }
    
    fun processAudioFrame(audioData: ShortArray) {
        val state = _currentState.value
        if (state != InterviewState.WAITING_FOR_USER && state != InterviewState.USER_SPEAKING) return
        
        val prob = vadClient.processFrameMock(audioData)
        
        if (prob > 0.5f) {
            if (state == InterviewState.WAITING_FOR_USER) {
                _currentState.value = InterviewState.USER_SPEAKING
            }
            silenceFramesCount = 0
        } else {
            if (state == InterviewState.USER_SPEAKING) {
                silenceFramesCount++
                if (silenceFramesCount > silenceThresholdFrames) {
                    // Turn complete
                    submitUserResponse("User spoken audio processed...") // In real app, transcribe audio
                }
            }
        }
    }

    fun submitUserResponse(response: String) {
        if (_currentState.value != InterviewState.WAITING_FOR_USER && _currentState.value != InterviewState.USER_SPEAKING) return
        
        _transcript.update { it + InterviewTurn("User", response) }
        _currentState.value = InterviewState.ANALYZING_RESPONSE
        silenceFramesCount = 0
        turnCount++
        
        // Simulate analysis
        if (turnCount >= maxTurns) {
            concludeSession()
        } else {
            val followup = generateFollowUp(response)
            startNextTurn(followup)
        }
    }

    private fun generateFollowUp(userResponse: String): String {
        return "That's interesting. Could you elaborate on the specific metrics or outcomes associated with that?"
    }

    private fun concludeSession() {
        _currentState.value = InterviewState.CONCLUDING
        _transcript.update { it + InterviewTurn("Interviewer", "Thank you for your time. We will review your performance and get back to you with detailed feedback.") }
        
        currentReport = scoringEngine.processSession(_transcript.value)
        vadClient.closeVadModelMock()
        
        _currentState.value = InterviewState.FINISHED
    }
    
    fun getReport(): InterviewReport? = currentReport
    
    fun requestHint() {
        if (_currentState.value == InterviewState.WAITING_FOR_USER || _currentState.value == InterviewState.USER_SPEAKING) {
            _transcript.update { it + InterviewTurn("Hint", "Consider structuring your answer using the STAR method: Situation, Task, Action, Result.") }
        }
    }
}
