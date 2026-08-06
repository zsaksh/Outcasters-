package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewFeedbackScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Interview Diagnostic", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("learn") { popUpTo("learn") { inclusive = true } } }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Executive Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Executive Summary", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Recommendation: ", fontSize = 16.sp, color = TextPrimary)
                            Text("LEAN HIRE", fontSize = 18.sp, color = AccentTeal, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ScoreItem("Structure", "85/100", AccentTeal)
                            ScoreItem("Technical", "90/100", AccentTeal)
                            ScoreItem("Comm.", "65/100", Color(0xFFF44336)) // Red
                            ScoreItem("Impact", "75/100", Color(0xFFFFC107)) // Yellow
                        }
                    }
                }
            }

            item {
                Text("Interactive Transcript Critique", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                CritiqueCard(
                    originalText = "We did the migration to AWS and it made things faster.",
                    critique = "Vague ownership and missing metrics. Use the STAR method.",
                    suggestion = "I led the migration to AWS, which reduced latency by 40%."
                )
                Spacer(modifier = Modifier.height(8.dp))
                CritiqueCard(
                    originalText = "Like, I guess I would probably use Redis or something.",
                    critique = "Filler words reduce confidence. Be decisive.",
                    suggestion = "I would use Redis for the caching layer because of its low latency."
                )
            }
            
            item {
                Text("Action Plan & Drills", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                ActionDrillCard("Practice STAR Method", "Focus on the 'Action' and 'Result' phases.")
                Spacer(modifier = Modifier.height(8.dp))
                ActionDrillCard("System Design Patterns", "Review caching and load balancing trade-offs.")
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ScoreItem(label: String, score: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(score, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun CritiqueCard(originalText: String, critique: String, suggestion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFFF44336), textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) {
                    append(originalText)
                }
            }
            Text(annotatedString, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF44336).copy(alpha = 0.1f)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡 ", fontSize = 14.sp)
                Text(critique, color = Color(0xFFD32F2F), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Better way to say it:", fontSize = 12.sp, color = TextSecondary)
            Text(suggestion, fontSize = 15.sp, color = AccentTeal, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ActionDrillCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 13.sp, color = TextSecondary)
            }
            Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Transparent) // Placeholder for alignment
        }
    }
}
