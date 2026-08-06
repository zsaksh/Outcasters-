package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.AiVoiceState
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMockInterviewScreen(navController: NavController) {
    var isListening by remember { mutableStateOf(false) }
    var voiceState by remember { mutableStateOf(AiVoiceState.SPEAKING) }
    var transcript by remember { mutableStateOf(listOf(
        "Interviewer" to "Welcome. To start, could you walk me through your experience with designing large-scale distributed systems?"
    )) }
    
    LaunchedEffect(Unit) {
        delay(3000)
        voiceState = AiVoiceState.LISTENING
        isListening = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mock Interview", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("mock_interview_setup") { popUpTo("live_mock_interview") { inclusive = true } } }) {
                        Icon(Icons.Filled.Close, contentDescription = "End Call", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate("interview_feedback") { popUpTo("live_mock_interview") { inclusive = true } } }) {
                        Text("Finish", color = AccentPurple, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Section - Target Role & Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Target: Senior ML Engineer @ Google", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Visualization
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍💼", fontSize = 60.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                AudioWaveformVisualizer(state = voiceState)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when(voiceState) {
                        AiVoiceState.SPEAKING -> "Interviewer is speaking..."
                        AiVoiceState.LISTENING -> "Listening to you..."
                        AiVoiceState.PROCESSING -> "Analyzing answer..."
                        AiVoiceState.IDLE -> "Paused"
                    },
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Need 30s to think */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Give me 30s", color = TextPrimary)
                }
                OutlinedButton(
                    onClick = { /* Ask for hint */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ask for Hint", color = TextPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Real-time Live Transcript Toggle area (Just showing the latest transcript text)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .heightIn(max = 150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transcript) { (speaker, text) ->
                        Row {
                            Text("$speaker: ", fontWeight = FontWeight.Bold, color = if (speaker == "Interviewer") AccentPurple else AccentTeal, fontSize = 14.sp)
                            Text(text, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { 
                        isListening = !isListening 
                        voiceState = if (isListening) AiVoiceState.LISTENING else AiVoiceState.IDLE
                    },
                    containerColor = if (isListening) Color(0xFFF44336) else AccentPurple,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Toggle Mic",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
