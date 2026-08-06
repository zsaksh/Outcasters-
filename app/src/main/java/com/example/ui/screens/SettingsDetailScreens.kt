package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(title: String, navController: NavController, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun BehaviorSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "AI Behavior", navController = navController) {
        Text("Configure how the AI responds and behaves.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        var responseLength by remember { mutableStateOf(0.5f) }
        Text("Response Length", color = TextPrimary)
        Slider(value = responseLength, onValueChange = { responseLength = it }, colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple))
        
        Spacer(modifier = Modifier.height(16.dp))
        var strictAcademic by remember { mutableStateOf(true) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Strict Academic Tone", color = TextPrimary)
            Switch(checked = strictAcademic, onCheckedChange = { strictAcademic = it }, colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple, checkedTrackColor = AccentPurple.copy(alpha=0.5f)))
        }
    }
}

@Composable
fun AppearanceSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "Appearance", navController = navController) {
        Text("Theme and UI preferences.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        var darkMode by remember { mutableStateOf(true) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dark Mode", color = TextPrimary)
            Switch(checked = darkMode, onCheckedChange = { darkMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple, checkedTrackColor = AccentPurple.copy(alpha=0.5f)))
        }
    }
}

@Composable
fun PerformanceSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "Performance", navController = navController) {
        Text("Tune inference performance.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        var threadCount by remember { mutableStateOf(4f) }
        Text("Thread Count: ${threadCount.toInt()}", color = TextPrimary)
        Slider(value = threadCount, onValueChange = { threadCount = it }, valueRange = 1f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = AccentOrange, activeTrackColor = AccentOrange))
    }
}

@Composable
fun PrivacySettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "Privacy", navController = navController) {
        Text("Manage your data and privacy settings.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        var localOnly by remember { mutableStateOf(true) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Local-Only Mode", color = TextPrimary)
            Switch(checked = localOnly, onCheckedChange = { localOnly = it }, colors = SwitchDefaults.colors(checkedThumbColor = AccentTeal, checkedTrackColor = AccentTeal.copy(alpha=0.5f)))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = TextPrimary)) {
            Text("Clear Chat History")
        }
    }
}

@Composable
fun StorageSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "Storage", navController = navController) {
        Text("Manage downloaded models and cache.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = TextPrimary)) {
            Text("Clear Model Cache")
        }
    }
}

@Composable
fun OCRSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "OCR", navController = navController) {
        Text("Configure document and image scanning.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        var autoCrop by remember { mutableStateOf(true) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Auto Crop Images", color = TextPrimary)
            Switch(checked = autoCrop, onCheckedChange = { autoCrop = it }, colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = TextPrimary.copy(alpha=0.5f)))
        }
    }
}

@Composable
fun AboutSettingsScreen(navController: NavController) {
    SettingsDetailScreen(title = "About Outcasters", navController = navController) {
        Text("Outcasters v1.0.0", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Text("Local AI for learning.", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        Text("A local-first, serverless, on-device AI academic companion.", color = TextPrimary)
    }
}
