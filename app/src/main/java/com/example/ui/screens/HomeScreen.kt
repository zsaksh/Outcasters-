package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.OutcastersApplication
import com.example.backend.models.ModelState
import com.example.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as OutcastersApplication
    val inferenceEngine = app.container.inferenceEngine
    val modelState by inferenceEngine.modelState.collectAsState()


    val recentSessions by app.container.chatDao.getAllSessions().collectAsState(initial = emptyList())

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Psychology, contentDescription = "Logo", tint = AccentTeal, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Outcasters", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                        }
                        Text("Local AI for learning", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                    }

                    // Status Pill
                    Surface(
                        color = when (modelState) {
                            is ModelState.Active -> AccentTeal.copy(alpha = 0.15f)
                            is ModelState.Ready -> AccentTeal.copy(alpha = 0.15f)
                            is ModelState.Loading -> AccentPurple.copy(alpha = 0.15f)
                            else -> Color.Red.copy(alpha = 0.15f)
                        },
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (modelState) {
                                            is ModelState.Active -> AccentTeal
                                            is ModelState.Ready -> AccentTeal
                                            is ModelState.Loading -> AccentPurple
                                            else -> Color.Red
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (modelState) {
                                    is ModelState.Active -> "Model Ready"
                                    is ModelState.Ready -> "Model Ready"
                                    is ModelState.Loading -> "Loading"
                                    else -> "Offline"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (modelState) {
                                    is ModelState.Active -> AccentTeal
                                    is ModelState.Ready -> AccentTeal
                                    is ModelState.Loading -> AccentPurple
                                    else -> Color.Red
                                }
                            )
                        }
                    }
                }
            }

            // Primary Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable { navController.navigate("scan") },
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        SurfaceVariantDark,
                                        Color(0xFF222B30) // subtle dark teal blend
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, "Scan", tint = Color.Black, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Ask or Scan", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Extract text, solve doubts and get instant answers", fontSize = 14.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ChevronRight, "Go", tint = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Quick Actions Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Lightbulb, title = "Learn", subtitle = "Concepts", iconTint = AccentTeal, onClick = { navController.navigate("learn") })
                        QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.ChatBubbleOutline, title = "Chat", subtitle = "With AI", iconTint = AccentPurple, onClick = { navController.navigate("chat") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Language, title = "Language", subtitle = "Practice", iconTint = AccentOrange, onClick = { navController.navigate("learn") })
                        QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Psychology, title = "Interview", subtitle = "Prep", iconTint = AccentTeal, onClick = { navController.navigate("learn") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard(modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.Settings, title = "Models Hub", subtitle = "Manage local AI", iconTint = Color.Gray, onClick = { navController.navigate("models") })
                    }
                }
            }

            // Recent Activity
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
                    Text("See all", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.clickable { navController.navigate("library") })
                }
            }


            item {

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filteredSessions = recentSessions.filter { 
                        it.title.isNotBlank() && !it.title.contains("Context from", ignoreCase = true) && !it.title.contains("Debug", ignoreCase = true)
                    }
                    if (filteredSessions.isEmpty()) {
                        Text("No recent activity", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        filteredSessions.take(3).forEach { session ->
                            RecentActivityItem(

                                title = session.title,
                                time = android.text.format.DateUtils.getRelativeTimeSpanString(session.timestamp).toString(),
                                onClick = { navController.navigate("chat/${session.id}") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier, icon: ImageVector, title: String, subtitle: String, iconTint: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
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
            Icon(icon, title, tint = iconTint, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextPrimary)
                Text(subtitle, fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun RecentActivityItem(title: String, time: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.History, contentDescription = "History", tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(time, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
