package com.example.backend.interview

import org.json.JSONArray
import org.json.JSONObject

data class InterviewReport(
    val structureScore: Int,
    val technicalScore: Int,
    val communicationScore: Int,
    val impactScore: Int,
    val overallRecommendation: String,
    val feedbackJson: String
)

class ScoringEngine {

    fun processSession(transcript: List<InterviewTurn>): InterviewReport {
        // Simplified scoring logic based on transcript characteristics
        var structureScore = 80
        var technicalScore = 85
        var communicationScore = 70
        var impactScore = 75

        // Check for filler words
        val userTurns = transcript.filter { it.speaker == "User" }
        val fillerCount = userTurns.sumOf { turn ->
            turn.message.split(" ").count { it.lowercase() in listOf("like", "um", "uh", "you know") }
        }
        
        communicationScore -= (fillerCount * 2).coerceAtMost(30)
        
        // Ensure bounds
        structureScore = structureScore.coerceIn(0, 100)
        technicalScore = technicalScore.coerceIn(0, 100)
        communicationScore = communicationScore.coerceIn(0, 100)
        impactScore = impactScore.coerceIn(0, 100)

        val averageScore = (structureScore + technicalScore + communicationScore + impactScore) / 4
        
        val recommendation = when {
            averageScore >= 85 -> "Strong Hire"
            averageScore >= 75 -> "Lean Hire"
            averageScore >= 60 -> "Lean No Hire"
            else -> "No Hire"
        }

        val feedbackJson = buildFeedbackJson(transcript, fillerCount)

        return InterviewReport(
            structureScore = structureScore,
            technicalScore = technicalScore,
            communicationScore = communicationScore,
            impactScore = impactScore,
            overallRecommendation = recommendation,
            feedbackJson = feedbackJson
        )
    }

    private fun buildFeedbackJson(transcript: List<InterviewTurn>, fillerCount: Int): String {
        val root = JSONObject()
        val critiques = JSONArray()
        
        // Add some mock critiques for demonstration
        if (fillerCount > 0) {
            val critique = JSONObject().apply {
                put("originalText", "Like, I guess I would probably use Redis or something.")
                put("critique", "Filler words reduce confidence. Be decisive.")
                put("suggestion", "I would use Redis for the caching layer because of its low latency.")
            }
            critiques.put(critique)
        }
        
        val critique2 = JSONObject().apply {
            put("originalText", "We did the migration to AWS and it made things faster.")
            put("critique", "Vague ownership and missing metrics. Use the STAR method.")
            put("suggestion", "I led the migration to AWS, which reduced latency by 40%.")
        }
        critiques.put(critique2)

        root.put("critiques", critiques)
        
        val drills = JSONArray()
        drills.put(JSONObject().apply {
            put("title", "Practice STAR Method")
            put("subtitle", "Focus on the 'Action' and 'Result' phases.")
        })
        root.put("drills", drills)

        return root.toString()
    }
}
