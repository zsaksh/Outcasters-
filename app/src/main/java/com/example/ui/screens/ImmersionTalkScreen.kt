package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*
import com.example.ui.components.AiVoiceState
import com.example.ui.components.AudioWaveformVisualizer

data class ImmersionPersona(
    val name: String,
    val role: String,
    val language: String,
    val cefr: String,
    val emoji: String,
    val initialSubtitle: String,
    val hints: List<String>
)

val availablePersonas = listOf(
    ImmersionPersona("Camille", "Parisian Barista", "French", "A2", "👩‍🎨", "Bonjour! Comment ça va aujourd'hui?", listOf("Ça va bien, merci.", "Un café, s'il vous plaît.", "Je ne comprends pas.")),
    ImmersionPersona("Mateo", "Madrid Guide", "Spanish", "B1", "👨‍🏫", "¡Hola! ¿Qué tal tu día?", listOf("Muy bien, gracias.", "¿Dónde está el museo?", "Más despacio, por favor.")),
    ImmersionPersona("Klaus", "Berlin Tech Bro", "German", "B2", "👨‍💻", "Hallo! Wie geht's dir heute?", listOf("Mir geht es gut.", "Ich brauche einen Kaffee.", "Können Sie das wiederholen?")),
    ImmersionPersona("Yuki", "Tokyo Student", "Japanese", "A1", "👩‍🎓", "こんにちは！元気ですか？", listOf("はい、元気です。", "これはいくらですか？", "わかりません。")),
    ImmersionPersona("Giulia", "Rome Chef", "Italian", "A2", "👩‍🍳", "Ciao! Come stai?", listOf("Sto bene, grazie.", "Un gelato, per favore.", "Non capisco.")),
    ImmersionPersona("Wei", "Beijing Driver", "Mandarin", "B1", "🚕", "你好！你去哪里？", listOf("我去机场。", "多少钱？", "太贵了。")),
    ImmersionPersona("Anya", "Moscow Artist", "Russian", "A1", "👩‍🎨", "Привет! Как дела?", listOf("Хорошо, спасибо.", "Где метро?", "Я не понимаю.")),
    ImmersionPersona("Sven", "Stockholm Designer", "Swedish", "B2", "👨‍🎨", "Hej! Hur mår du?", listOf("Jag mår bra.", "En kaffe, tack.", "Vad sa du?")),
    ImmersionPersona("Ji-Hoon", "Seoul Gamer", "Korean", "B1", "🎮", "안녕하세요! 잘 지내요?", listOf("네, 잘 지내요.", "얼마예요?", "이해 못했어요.")),
    ImmersionPersona("Fatima", "Dubai Guide", "Arabic", "A2", "🧕", "مرحباً! كيف حالك؟", listOf("أنا بخير، شكراً.", "أين الحمام؟", "لا أفهم.")),
    ImmersionPersona("Raj", "Mumbai Engineer", "Hindi", "B1", "👨‍💻", "नमस्ते! आप कैसे हैं?", listOf("मैं ठीक हूँ।", "कितने पैसे?", "मुझे समझ नहीं आया।")),
    ImmersionPersona("Liam", "Dublin Musician", "Irish", "A1", "🎸", "Dia duit! Conas atá tú?", listOf("Tá mé go maith.", "Go raibh maith agat.", "Ní thuigim.")),
    ImmersionPersona("Maria", "Lisbon Florist", "Portuguese", "A2", "🌷", "Olá! Como está?", listOf("Estou bem, obrigado.", "Quanto custa?", "Pode repetir?")),
    ImmersionPersona("Lukas", "Vienna Baker", "Austrian German", "B1", "🥨", "Servus! Wie geht's?", listOf("Gut, danke.", "Ein Stück Kuchen, bitte.", "Ich verstehe nicht.")),
    ImmersionPersona("Noor", "Istanbul Student", "Turkish", "A2", "👩‍🎓", "Merhaba! Nasılsın?", listOf("İyiyim, teşekkürler.", "Ne kadar?", "Anlamıyorum.")),
    ImmersionPersona("Aris", "Athens Historian", "Greek", "B2", "🏛️", "Γεια σας! Τι κάνετε;", listOf("Είμαι καλά.", "Πού είναι το μουσείο;", "Δεν καταλαβαίνω.")),
    ImmersionPersona("Mila", "Prague Librarian", "Czech", "A1", "📚", "Ahoj! Jak se máš?", listOf("Mám se dobře.", "Kde je toaleta?", "Nerozumím.")),
    ImmersionPersona("Jan", "Amsterdam Cyclist", "Dutch", "B1", "🚲", "Hallo! Hoe gaat het?", listOf("Het gaat goed.", "Een biertje, alsjeblieft.", "Wat zeg je?")),
    ImmersionPersona("Nia", "Nairobi Entrepreneur", "Swahili", "A2", "💼", "Jambo! Habari gani?", listOf("Nzuri sana.", "Ni bei gani?", "Sielewi.")),
    ImmersionPersona("Linh", "Hanoi Food Vender", "Vietnamese", "B1", "🍲", "Xin chào! Bạn có khỏe không?", listOf("Tôi khỏe, cảm ơn.", "Phở bao nhiêu tiền?", "Tôi không hiểu."))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmersionTalkScreen(navController: NavController) {
    var isListening by remember { mutableStateOf(false) }
    var showHints by remember { mutableStateOf(false) }
    var selectedPersona by remember { mutableStateOf(availablePersonas[0]) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("ImmersionTalk", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("debrief") { popUpTo("immersion_talk") { inclusive = true } } }) {
                        Icon(Icons.Filled.Close, contentDescription = "End Call", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Selection
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availablePersonas) { persona ->
                    FilterChip(
                        selected = selectedPersona == persona,
                        onClick = { 
                            selectedPersona = persona
                            showHints = false
                        },
                        label = { Text(persona.language) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                            selectedLabelColor = AccentPurple
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Persona UI
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AccentTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(selectedPersona.emoji, fontSize = 64.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("${selectedPersona.name} (${selectedPersona.role})", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
            Text("CEFR ${selectedPersona.cefr} • ${selectedPersona.language}", fontSize = 14.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Interactive Subtitles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tap any word to translate", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = selectedPersona.initialSubtitle,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Hints / Scaffolding
            AnimatedVisibility(visible = showHints) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedPersona.hints.forEach { hint ->
                        HintChip(hint)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showHints = !showHints },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Filled.HelpOutline, contentDescription = "Help Me Answer", tint = TextPrimary)
                }
                
                // Mic Button
                FloatingActionButton(
                    onClick = { isListening = !isListening },
                    containerColor = if (isListening) AccentOrange else AccentPurple,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Speaking",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                // Placeholder for symmetry
                Box(modifier = Modifier.size(56.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            val voiceState = if (isListening) AiVoiceState.LISTENING else AiVoiceState.IDLE
            AudioWaveformVisualizer(state = voiceState)
        }
    }
}

@Composable
fun HintChip(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Send text */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}
