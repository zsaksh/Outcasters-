package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.backend.settings.SettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val autoActivate by settingsManager.autoActivateModels.collectAsState(initial = true)
    val offlineOnly by settingsManager.offlineOnlyMode.collectAsState(initial = false)
    val memorySaver by settingsManager.memorySaverMode.collectAsState(initial = false)
    val webSearch by settingsManager.realTimeWebSearch.collectAsState(initial = true)
    var contextLength by remember { mutableStateOf(4096f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingSwitchRow(
                title = "Auto-activate after download",
                subtitle = "Automatically load models into memory when downloaded",
                checked = autoActivate,
                onCheckedChange = { 
                    coroutineScope.launch { settingsManager.setAutoActivateModels(it) } 
                }
            )
            
            SettingSwitchRow(
                title = "Offline-only mode",
                subtitle = "Never connect to the internet",
                checked = offlineOnly,
                onCheckedChange = { 
                    coroutineScope.launch { settingsManager.setOfflineOnlyMode(it) } 
                }
            )
            
            SettingSwitchRow(
                title = "Memory Saver mode",
                subtitle = "Prioritize RAM over generation speed",
                checked = memorySaver,
                onCheckedChange = { 
                    coroutineScope.launch { settingsManager.setMemorySaverMode(it) } 
                }
            )
            
            SettingSwitchRow(
                title = "Real-time web search",
                subtitle = "Allow the AI to fetch recent information",
                checked = webSearch,
                onCheckedChange = { 
                    coroutineScope.launch { settingsManager.setRealTimeWebSearch(it) } 
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Context length", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = contextLength,
                onValueChange = { contextLength = it },
                valueRange = 1024f..8192f,
                steps = 7
            )
            Text("${contextLength.toInt()} tokens", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Data Management", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = { navController.navigate("model_management") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Manage Downloaded Models")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        val container = (context.applicationContext as com.example.OutcastersApplication).container
                        container.chatDao.deleteAllMessages()
                        container.chatDao.deleteAllSessions()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Learning History")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        val container = (context.applicationContext as com.example.OutcastersApplication).container
                        container.chatDao.deleteAllMessages()
                        container.chatDao.deleteAllSessions()
                        container.modelManifestDao.deleteAllModels()
                        settingsManager.clearAll()
                        // Delete downloaded files
                        val modelsDir = java.io.File(context.filesDir, "models")
                        if (modelsDir.exists()) {
                            modelsDir.deleteRecursively()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset App")
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
