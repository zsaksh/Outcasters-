package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.backend.device.SystemUtility
import com.example.backend.hf.HFDownloadWorker
import com.example.backend.hf.HuggingFaceApi
import com.example.backend.models.ModelManifest
import com.example.data.SrsDatabase
import com.example.ui.viewmodels.ModelManagerViewModel
import com.example.ui.viewmodels.ModelManagerViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelHubScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { androidx.room.Room.databaseBuilder(context, SrsDatabase::class.java, "srs_db").fallbackToDestructiveMigration().build() }
    val viewModel: ModelManagerViewModel = viewModel(factory = ModelManagerViewModelFactory(db.modelManifestDao()))
    
    val activeModel by viewModel.activeModel.collectAsState()
    val allModels by viewModel.allModels.collectAsState(initial = emptyList())
    
    val systemUtility = remember { SystemUtility(context) }
    val maxRamMb = systemUtility.getMaxAllocatedRamMb()
    val availableRamMb = systemUtility.getSystemSpecs().availableRamMb
    
    var tabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val hfApi = remember { HuggingFaceApi() }
    var searchResults by remember { mutableStateOf<List<com.example.backend.hf.HFModelInfo>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    var showWarningDialog by remember { mutableStateOf<ModelManifest?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Local", fontSize = 12.sp) })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Hugging Face", fontSize = 12.sp) })
                Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }, text = { Text("Cache", fontSize = 12.sp) })
            }
            
            if (tabIndex == 0) {
                // LOCAL MODELS TAB
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        if (activeModel != null) {
                            ActiveModelHero(activeModel!!) { viewModel.activateModel("") /* Simulate unload */ }
                        }
                    }
                    
                    item { Text("Downloaded", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    
                    val downloaded = allModels.filter { it.downloadStatus == "downloaded" && it.modelId != activeModel?.modelId }
                    if (downloaded.isEmpty()) {
                        item {
                            Text("No downloaded models. Go to Hugging Face tab to find some.", color = Color.Gray)
                        }
                    } else {
                        items(downloaded) { model ->
                            val estimatedRam = model.fileSizeBytes / (1024 * 1024)
                            val isRecommended = systemUtility.canSafelyLoadModel(estimatedRam, model.contextLength)
                            LocalModelCard(
                                model = model, 
                                isRecommended = isRecommended,
                                onActivate = { 
                                    if (!isRecommended) {
                                        showWarningDialog = model
                                    } else {
                                        viewModel.activateModel(model.modelId)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (tabIndex == 1) {
                // HUGGING FACE TAB
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search HF Repos or Paste URL") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    isSearching = true
                                    searchResults = hfApi.searchGGUFModels(searchQuery)
                                    isSearching = false
                                }
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Featured defaults if search is empty
                            if (searchResults.isEmpty() && searchQuery.isBlank()) {
                                item { Text("Featured Models for Mobile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                                val featured = listOf(
                                    com.example.backend.hf.HFModelInfo("bartowski/Llama-3.2-1B-Instruct-GGUF", "bartowski", 5000, 150),
                                    com.example.backend.hf.HFModelInfo("bartowski/Qwen2.5-1.5B-Instruct-GGUF", "bartowski", 4500, 120),
                                    com.example.backend.hf.HFModelInfo("bartowski/Phi-3.5-mini-instruct-GGUF", "bartowski", 6000, 200),
                                    com.example.backend.hf.HFModelInfo("bartowski/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "bartowski", 12000, 400)
                                )
                                items(featured) { result ->
                                    HFResultCard(result) {
                                        startDownload(context, db.modelManifestDao(), result)
                                    }
                                }
                            } else {
                                items(searchResults) { result ->
                                    HFResultCard(result) {
                                        startDownload(context, db.modelManifestDao(), result)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tabIndex == 2) {
                ModelCacheManagerTab(allModels = allModels, onDelete = { viewModel.deleteModel(context, it) })
            }
        }
        
        if (showWarningDialog != null) {
            AlertDialog(
                onDismissRequest = { showWarningDialog = null },
                title = { Text("Memory Warning") },
                text = {
                    val required = showWarningDialog!!.fileSizeBytes / (1024 * 1024)
                    Text("This model requires ~$required MB RAM. Your phone has ~$availableRamMb MB free. Loading may cause lag or crash. \n\nWe recommend a lighter quantization (like IQ3_XS).")
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.activateModel(showWarningDialog!!.modelId)
                        showWarningDialog = null
                    }) {
                        Text("Load Anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWarningDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun startDownload(context: android.content.Context, dao: com.example.backend.models.ModelManifestDao, info: com.example.backend.hf.HFModelInfo) {
    kotlinx.coroutines.GlobalScope.launch {
        val api = HuggingFaceApi()
        val files = api.getModelFiles(info.id)
        val bestFile = files.find { it.path.contains("Q4_K_M") || it.path.contains("Q4_K_S") } ?: files.firstOrNull()
        
        if (bestFile != null) {
            val modelId = UUID.randomUUID().toString()
            val manifest = ModelManifest(
                modelId = modelId,
                displayName = info.id.split("/").last().replace("-GGUF", ""),
                repoId = info.id,
                sourceUrl = bestFile.downloadUrl,
                fileName = bestFile.path,
                quantization = if (bestFile.path.contains("Q4_K")) "Q4_K" else "Unknown",
                fileSizeBytes = bestFile.size,
                downloadStatus = "not_installed"
            )
            dao.insert(manifest)
            
            val workRequest = OneTimeWorkRequestBuilder<HFDownloadWorker>()
                .setInputData(workDataOf(
                    "MODEL_ID" to modelId,
                    "DOWNLOAD_URL" to bestFile.downloadUrl,
                    "FILE_NAME" to bestFile.path
                ))
                .build()
                
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

@Composable
fun ActiveModelHero(model: ModelManifest, onUnload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACTIVE ENGINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(model.displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Quantization: ${model.quantization} | Context: ${model.contextLength}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Memory Footprint", fontSize = 12.sp)
                    Text("~${model.fileSizeBytes / (1024*1024)} MB", fontWeight = FontWeight.Bold)
                }
                Button(onClick = onUnload, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Unload")
                }
            }
        }
    }
}

@Composable
fun LocalModelCard(model: ModelManifest, isRecommended: Boolean, onActivate: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${model.fileSizeBytes / (1024*1024)} MB • ${model.quantization} • Est RAM: ${model.fileSizeBytes / (1024*1024) + 500} MB", fontSize = 12.sp, color = Color.Gray)
                }
                Button(onClick = onActivate) {
                    Text("Load")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isRecommended) {
                AssistChip(
                    onClick = { },
                    label = { Text("Recommended for your device", fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = MaterialTheme.colorScheme.primary),
                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            } else {
                AssistChip(
                    onClick = { },
                    label = { Text("May exceed device memory", fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = MaterialTheme.colorScheme.error),
                    leadingIcon = { Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun HFResultCard(info: com.example.backend.hf.HFModelInfo, onDownload: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(info.id, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("by ${info.author}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" ${info.downloads}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" ${info.likes}", fontSize = 12.sp)
                }
                Button(onClick = onDownload) {
                    Text("Download")
                }
            }
        }
    }
}

@Composable
fun ModelCacheManagerTab(allModels: List<ModelManifest>, onDelete: (String) -> Unit) {
    val downloadedModels = allModels.filter { it.downloadStatus == "downloaded" || it.downloadStatus == "not_installed" }
    // We only care about actually downloaded models. The not_installed ones are there, but their file might be deleted. 
    // Wait, let's filter correctly:
    val storedModels = allModels.filter { it.fileSizeBytes > 0 && it.downloadStatus == "downloaded" }
    
    val totalSize = storedModels.sumOf { it.fileSizeBytes }
    val totalSizeMB = totalSize / (1024 * 1024)
    val totalSizeGB = totalSizeMB / 1024f
    
    val architectureMap = storedModels.groupBy { 
        val name = it.displayName.lowercase()
        when {
            name.contains("llama") -> "Llama"
            name.contains("qwen") -> "Qwen"
            name.contains("phi") -> "Phi"
            name.contains("gemma") -> "Gemma"
            name.contains("deepseek") -> "DeepSeek"
            else -> "Other"
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Disk Usage", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(if (totalSizeGB > 1f) String.format("%.2f GB", totalSizeGB) else "$totalSizeMB MB", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Breakdown by Architecture", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                architectureMap.forEach { (arch, models) ->
                    val size = models.sumOf { it.fileSizeBytes } / (1024 * 1024)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(arch, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("$size MB", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Stored Files", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (storedModels.isEmpty()) {
            Text("No models stored on disk.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(storedModels) { model ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.displayName, fontWeight = FontWeight.Bold)
                                Text("${model.fileSizeBytes / (1024*1024)} MB", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (model.activeStatus) {
                                Text("Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                            } else {
                                IconButton(onClick = { onDelete(model.modelId) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Clear Cache", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
