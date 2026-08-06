package com.example.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.OutcastersApplication
import com.example.backend.models.ModelManifest
import com.example.ui.viewmodels.ModelManagerViewModel
import com.example.ui.viewmodels.ModelManagerViewModelFactory
import android.app.ActivityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelHubScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as OutcastersApplication
    val container = app.container
    val viewModel: ModelManagerViewModel = viewModel(
        factory = ModelManagerViewModelFactory(container.modelManifestDao, container.downloadManager)
    )

    val allModels by viewModel.allModels.collectAsState(initial = emptyList())
    val activeModel by viewModel.activeModel.collectAsState()

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamMB = (memoryInfo.totalMem / (1024 * 1024)).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Models") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ActiveModelSummary(activeModel)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Compatible Models", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(allModels) { model ->
                    ModelCard(
                        model = model,
                        totalRamMB = totalRamMB,
                        onDownload = { viewModel.startDownload(model.modelId) },
                        onPause = { viewModel.pauseDownload(model.modelId) },
                        onResume = { viewModel.retryDownload(model.modelId) },
                        onRetry = { viewModel.retryDownload(model.modelId) },
                        onActivate = { viewModel.activateModel(model.modelId) },
                        onDelete = { viewModel.deleteModel(context, model.modelId) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveModelSummary(activeModel: ModelManifest?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Model", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            if (activeModel != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(activeModel.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("RAM Usage: ~${activeModel.estimatedRamMB} MB", style = MaterialTheme.typography.bodySmall)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No model loaded", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select a model below to use the chat.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ModelCard(
    model: ModelManifest,
    totalRamMB: Int,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompatible = model.estimatedRamMB < (totalRamMB * 0.7)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(model.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (model.activeStatus) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Active") }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val sizeStr = if (model.fileSizeBytes > 0) "${model.fileSizeBytes / (1024 * 1024)} MB • " else ""
                    Text("${sizeStr}Est RAM: ${model.estimatedRamMB} MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isCompatible) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("May exceed device memory", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (model.compatibilityFlag == "Lite" || model.compatibilityFlag == "Standard") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recommended for this device", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status and Actions
            when (model.installStatus) {
                "not_installed" -> {
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download")
                    }
                }
                "downloading" -> {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Downloading...", fontSize = 12.sp)
                            Text("${model.downloadProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { model.downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onPause) {
                                Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause")
                            }
                        }
                    }
                }
                "paused" -> {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paused", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            Text("${model.downloadProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { model.downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = onResume, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume")
                            }
                            OutlinedButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
                "verifying" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying file...", fontSize = 14.sp)
                    }
                }
                "ready" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (!model.activeStatus) {
                            Button(onClick = onActivate, modifier = Modifier.weight(1f)) {
                                Text("Activate")
                            }
                        } else {
                            OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.weight(1f)) {
                                Text("Currently Active")
                            }
                        }
                        OutlinedButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                "failed", "corrupted" -> {
                    Column {
                        Text("Error: ${model.errorState}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry")
                            }
                            OutlinedButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
