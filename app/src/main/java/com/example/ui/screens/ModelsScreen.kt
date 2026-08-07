package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.OutcastersApplication
import com.example.ui.theme.*
import com.example.ui.viewmodels.ModelManagerViewModel
import com.example.ui.viewmodels.ModelManagerViewModelFactory
import com.example.backend.models.ModelManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ModelsScreen(
    navController: NavController
) {
    val context = LocalContext.current.applicationContext as OutcastersApplication
    val modelManagerViewModel: ModelManagerViewModel = viewModel(
        factory = ModelManagerViewModelFactory(
            context.container.modelManifestDao,
            context.container.downloadManager
        )
    )
    val localContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }

    val allModels by modelManagerViewModel.allModels.collectAsState(initial = emptyList())
    val activeModel by modelManagerViewModel.activeModel.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isImporting = true
            coroutineScope.launch {
                val success = importModelFile(localContext, it)
                isImporting = false
                if (success) {
                    Toast.makeText(localContext, "Model imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(localContext, "Failed to import model", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                Text("Models", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
                Text("Manage your local models", fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }

            if (activeModel != null) {
                item {
                    Text("Active Model", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    ActiveModelCard(activeModel!!)
                }
            }

            item {
                Text("Available Models", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(allModels) { model ->
                AvailableModelCard(
                    model = model,
                    onClick = {
                        if (model.installStatus == "not_installed" || model.installStatus == "failed") {
                            modelManagerViewModel.startDownload(model.modelId)
                        } else if (model.installStatus == "ready") {
                            modelManagerViewModel.activateModel(model.modelId)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.FolderZip,
                        text = if (isImporting) "Importing..." else "Import Model",
                        color = AccentTeal,
                        enabled = !isImporting,
                        onClick = { importLauncher.launch(arrayOf("*/*")) }
                    )

                    val githubIntent = remember {
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/google/mediapipe"))
                    }
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.AddLink,
                        text = "Find on GitHub",
                        color = AccentPurple,
                        onClick = { localContext.startActivity(githubIntent) }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

suspend fun importModelFile(context: Context, uri: Uri): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            var fileName = "imported_model.tflite"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
            }
            val destFile = File(modelsDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Composable
fun ActiveModelCard(model: ModelManifest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Psychology, "Model", tint = AccentTeal, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(model.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("${model.format} • ${model.quantization}", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, "Ready", tint = AccentTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ready", fontSize = 12.sp, color = AccentTeal, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active", fontSize = 12.sp, color = AccentTeal, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val sizeStr = String.format("%.2f GB", model.fileSizeBytes / (1024.0 * 1024.0 * 1024.0))
                ModelStat(icon = Icons.Filled.Storage, label = "Size", value = sizeStr)
                ModelStat(icon = Icons.Filled.Memory, label = "RAM", value = "${model.estimatedRamMB} MB")
            }
        }
    }
}

@Composable
fun ModelStat(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AvailableModelCard(model: ModelManifest, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Psychology, "Model", tint = AccentPurple, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(model.displayName, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                    Text("${model.format} • ${model.quantization}", fontSize = 12.sp, color = TextSecondary)
                    val sizeStr = String.format("%.2f GB", model.fileSizeBytes / (1024.0 * 1024.0 * 1024.0))
                    Text("$sizeStr • ${model.estimatedRamMB} MB RAM", fontSize = 12.sp, color = TextSecondary)
                }
            }
            if (model.installStatus == "ready") {
                Icon(Icons.Filled.CheckCircle, "Ready", tint = AccentTeal)
            } else if (model.installStatus == "downloading") {
                CircularProgressIndicator(
                    progress = { model.downloadProgress / 100f },
                    modifier = Modifier.size(24.dp),
                    color = AccentTeal
                )
            } else {
                Icon(Icons.Filled.CloudDownload, "Download", tint = TextSecondary)
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color,
            disabledContainerColor = color.copy(alpha = 0.05f),
            disabledContentColor = color.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(0.dp),
        enabled = enabled
    ) {
        Icon(icon, text, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
