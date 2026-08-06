package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

data class PhonemeScore(val text: String, val confidence: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebriefScreen(navController: NavController) {
    // Mock phoneme data for heatmap
    val phonemes = listOf(
        PhonemeScore("Je ", 0.95f),
        PhonemeScore("suis ", 0.85f),
        PhonemeScore("al", 0.40f), // Mispronounced
        PhonemeScore("lé ", 0.50f), // Mispronounced
        PhonemeScore("au ", 0.90f),
        PhonemeScore("ciné", 0.70f),
        PhonemeScore("ma.", 0.80f)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Post-Call Debrief", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pronunciation Radar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                PronunciationHeatmap(phonemes)
            }
            
            item {
                Text("Failed Concepts (Added to SRS)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                FailedConceptCard(concept = "passé_composé_avoir", userUsed = "j'ai allé", correct = "je suis allé")
                Spacer(modifier = Modifier.height(8.dp))
                FailedConceptCard(concept = "Vocabulary: croissant", userUsed = "la croissant", correct = "le croissant")
            }
        }
    }
}

@Composable
fun PronunciationHeatmap(phonemes: List<PhonemeScore>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val annotatedString = buildAnnotatedString {
            phonemes.forEach { phoneme ->
                val color = when {
                    phoneme.confidence >= 0.8f -> Color(0xFF4CAF50) // Green
                    phoneme.confidence >= 0.6f -> Color(0xFFFFC107) // Yellow
                    else -> Color(0xFFF44336) // Red
                }
                withStyle(style = SpanStyle(color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                    append(phoneme.text)
                }
            }
        }
        Text(
            text = annotatedString,
            modifier = Modifier.padding(16.dp),
            lineHeight = 28.sp
        )
    }
}

@Composable
fun FailedConceptCard(concept: String, userUsed: String, correct: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(concept, fontWeight = FontWeight.Bold, color = AccentOrange, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("You said: ", color = TextSecondary, fontSize = 14.sp)
                Text(userUsed, color = Color(0xFFF44336), fontSize = 14.sp) // Red
            }
            Row {
                Text("Correction: ", color = TextSecondary, fontSize = 14.sp)
                Text(correct, color = Color(0xFF4CAF50), fontSize = 14.sp) // Green
            }
        }
    }
}
