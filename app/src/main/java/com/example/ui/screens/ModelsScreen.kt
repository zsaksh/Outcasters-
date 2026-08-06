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
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ModelsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }

    val tiers = listOf("Lite", "Balanced", "Premium", "Multi")
    var selectedTier by remember { mutableStateOf("Balanced") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isImporting = true
            coroutineScope.launch {
                val success = importModelFile(context, it)
                isImporting = false
                if (success) {
                    Toast.makeText(context, "Model imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to import model", Toast.LENGTH_SHORT).show()
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

            item {
                Text("Recommended For You", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tiers.forEach { tier ->
                        val isSelected = selectedTier == tier
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentTeal.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedTier = tier }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tier,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) AccentTeal else TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text("Active Model", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                ActiveModelCard()
            }

            item {
                Text("Available Models", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                
                when (selectedTier) {
                    "Lite" -> {
                        AvailableModelCard("SmolLM2 360M Instruct", "GGUF • Q4_K_M", "340 MB • 1.0 GB RAM", isDownloaded = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        AvailableModelCard("Qwen 2.5 0.5B Instruct", "GGUF • Q4_K_M", "450 MB • 1.2 GB RAM", isDownloaded = false)
                    }
                    "Balanced" -> {
                        AvailableModelCard("Llama 3.2 1B Instruct", "GGUF • Q4_K_M", "1.3 GB • 1.8 GB RAM", isDownloaded = false)
                        Spacer(modifier = Modifier.height(12.dp))
                        AvailableModelCard("Gemma 2 2B Instruct", "GGUF • Q4_K_M", "1.8 GB • 2.5 GB RAM", isDownloaded = false)
                    }
                    "Premium" -> {
                        AvailableModelCard("Phi-3.5 Mini Instruct", "GGUF • Q4_K_M", "2.1 GB • 3.2 GB RAM", isDownloaded = false)
                        Spacer(modifier = Modifier.height(12.dp))
                        AvailableModelCard("Llama 3 8B Instruct", "GGUF • Q4_K_M", "4.5 GB • 6.0 GB RAM", isDownloaded = false)
                    }
                    "Multi" -> {
                        AvailableModelCard("Mistral Nemo 12B", "GGUF • Q4_K_M", "6.2 GB • 8.0 GB RAM", isDownloaded = false)
                        Spacer(modifier = Modifier.height(12.dp))
                        AvailableModelCard("Aya 23 8B", "GGUF • Q4_K_M", "4.5 GB • 6.0 GB RAM", isDownloaded = false)
                    }
                }
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
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/ggerganov/llama.cpp"))
                    }
                    ActionButton(
                        modifier = Modifier.weight(1f), 
                        icon = Icons.Filled.AddLink, 
                        text = "Find on GitHub", 
                        color = AccentPurple,
                        onClick = { context.startActivity(githubIntent) }
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
            var fileName = "imported_model.gguf"
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
fun ActiveModelCard() {
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
                    Text("Qwen 2.5 0.5B Instruct", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("GGUF • Q4_K_M", fontSize = 13.sp, color = TextSecondary)
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
                ModelStat(icon = Icons.Filled.Storage, label = "Size", value = "1.03 GB")
                ModelStat(icon = Icons.Filled.Memory, label = "RAM", value = "1.6 GB")
                ModelStat(icon = Icons.Filled.DataUsage, label = "Context", value = "32K")
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
fun AvailableModelCard(title: String, format: String, stats: String, isDownloaded: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { },
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
                    Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                    Text(format, fontSize = 12.sp, color = TextSecondary)
                    Text(stats, fontSize = 12.sp, color = TextSecondary)
                }
            }
            if (isDownloaded) {
                Icon(Icons.Filled.FileDownload, "Downloaded", tint = AccentTeal)
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
