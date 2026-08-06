package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewFeedbackListScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Feedback Reports", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recent Interviews", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                FeedbackListCard(
                    role = "Senior ML Engineer @ Google",
                    date = "Today",
                    score = "Lean Hire",
                    onClick = { navController.navigate("interview_feedback") }
                )
            }
            item {
                FeedbackListCard(
                    role = "Product Manager @ Meta",
                    date = "Yesterday",
                    score = "Strong Hire",
                    onClick = { navController.navigate("interview_feedback") }
                )
            }
            item {
                FeedbackListCard(
                    role = "Backend Engineer @ Stripe",
                    date = "Last Week",
                    score = "No Hire",
                    onClick = { navController.navigate("interview_feedback") }
                )
            }
        }
    }
}

@Composable
fun FeedbackListCard(role: String, date: String, score: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(role, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, fontSize = 13.sp, color = TextSecondary)
                    Text(" • ", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = score,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (score) {
                            "Strong Hire" -> AccentTeal
                            "Lean Hire" -> AccentTeal
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = "View", tint = TextSecondary)
        }
    }
}
