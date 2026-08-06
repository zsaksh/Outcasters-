package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.OutcastersApplication
import com.example.backend.models.ModelManifest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val container = (context.applicationContext as OutcastersApplication).container
    val coroutineScope = rememberCoroutineScope()
    
    val models by container.modelManifestDao.getAllModels().collectAsState(initial = emptyList())
    val downloadedModels = models.filter { it.downloadStatus == "downloaded" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Models") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (downloadedModels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No models downloaded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(downloadedModels) { model ->
                    ModelManagementCard(
                        model = model,
                        onDelete = {
                            coroutineScope.launch {
                                // Delete file
                                val modelsDir = File(context.filesDir, "models")
                                val modelFile = File(modelsDir, model.fileName)
                                if (modelFile.exists()) {
                                    modelFile.delete()
                                }
                                // Update status in db
                                container.modelManifestDao.update(
                                    model.copy(
                                        downloadStatus = "not_installed",
                                        installStatus = "pending",
                                        activeStatus = false
                                    )
                                )
                                // If it was the active model, unload from inference engine
                                if (model.activeStatus) {
                                    container.inferenceEngine.unloadModel()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelManagementCard(model: ModelManifest, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                val sizeMB = model.fileSizeBytes / (1024 * 1024)
                Text("${model.quantization} • $sizeMB MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Model", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
