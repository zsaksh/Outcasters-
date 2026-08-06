package com.example.ui.screens

import com.example.ui.components.PerformanceChart
import com.example.ui.components.ChartData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.*

import androidx.compose.ui.platform.LocalContext
import com.example.data.AppDatabase
import com.example.data.ChatMessageEntity

@Composable
fun LearnScreen(navController: NavController) {
    val context = LocalContext.current
    val container = (context.applicationContext as com.example.OutcastersApplication).container
    val mostRecentMessage by container.chatDao.getMostRecentMessage().collectAsState(initial = null)

    var selectedMode by remember { mutableStateOf("Concept") }
    val modes = listOf("Concept", "Language", "Interview")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Learn", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
                Text("Grow your knowledge step by step", fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }

            // Segmented Control
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    modes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentPurple.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedMode = mode }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) AccentPurple else TextSecondary
                            )
                        }
                    }
                }
            }

            // Mode Content Grid
            item {
                when (selectedMode) {
                    "Concept" -> ConceptGrid(navController)
                    "Language" -> LanguageGrid(navController)
                    "Interview" -> InterviewDashboard(navController)
                }
            }

            // Continue Learning
            if (mostRecentMessage != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Continue Learning", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
                        Text("See all", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.clickable { navController.navigate("library") })
                    }
                }

                item {
                    ContinueLearningCard(navController, mostRecentMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ConceptGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Explain Simply", tint = Color(0xFFFFB74D), route = "chat?mode=concept")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Step by Step", tint = AccentPurple, route = "chat?mode=concept")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Examples", tint = Color(0xFFAED581), route = "chat?mode=concept")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.CompareArrows, title = "Compare Topics", tint = AccentOrange, route = "chat?mode=concept")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.QuestionMark, title = "Quiz Me", tint = Color(0xFFF06292), route = "chat?mode=quiz")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Summarize, title = "Summarize", tint = AccentTeal, route = "chat?mode=concept")
        }
    }
}

@Composable
fun LanguageGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Translate", tint = AccentTeal, route = "chat?mode=translate")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Vocabulary", tint = AccentPurple, route = "chat?mode=vocabulary")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "Grammar", tint = AccentOrange, route = "chat?mode=grammar")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.CompareArrows, title = "ImmersionTalk", tint = Color(0xFFAED581), route = "immersion_talk")
        }
    }
}


@Composable
fun InterviewDashboard(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Active Target Role pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Target: Senior ML Engineer @ Google", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
        
        // Performance Trends Chart
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Performance Trends", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                PerformanceChart(
                    chartDataList = listOf(
                        ChartData(color = AccentTeal, points = listOf(60f, 65f, 75f, 85f, 90f)), // Technical
                        ChartData(color = AccentPurple, points = listOf(50f, 60f, 65f, 80f, 85f)), // Structure
                        ChartData(color = AccentOrange, points = listOf(70f, 68f, 72f, 75f, 78f))  // Communication
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem("Technical", AccentTeal)
                    LegendItem("Structure", AccentPurple)
                    LegendItem("Comm.", AccentOrange)
                }
            }
        }
        
        // Start Live Mock Interview
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("mock_interview_setup") },
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Live", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Live Mock Interview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Voice or Text input toggle", fontSize = 13.sp, color = TextSecondary)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = "Start", tint = AccentPurple)
            }
        }
        
        // Recent Interview Reports & Feedback
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("interview_feedback_list") },
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Summarize, contentDescription = "Reports", tint = AccentTeal)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Feedback Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("View past diagnostics", fontSize = 13.sp, color = TextSecondary)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = "View", tint = AccentTeal)
            }
        }

        // Quick Drill Cards
        Text("Quick Drills", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(top = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "System Design", tint = AccentOrange, route = "chat?mode=interview")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.FormatListNumbered, title = "Case Math", tint = Color(0xFFAED581), route = "chat?mode=interview")
            LearnActionCard(navController, modifier = Modifier.weight(1f), icon = Icons.Filled.AutoAwesome, title = "STAR Refiner", tint = AccentPurple, route = "chat?mode=interview")
        }
    }
}

@Composable
fun LearnActionCard(navController: NavController, modifier: Modifier, icon: ImageVector, title: String, tint: Color, route: String = "chat") {
    Card(
        modifier = modifier
            .height(100.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { navController.navigate(route) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, title, tint = tint, modifier = Modifier.size(24.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
        }
    }
}

@Composable
fun ContinueLearningCard(navController: NavController, mostRecentMessage: ChatMessageEntity?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { navController.navigate("chat") },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lightbulb, "Math", tint = AccentPurple, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mostRecentMessage?.content ?: "History",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text("Recent Chat", fontSize = 13.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChevronRight, "Continue", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}
