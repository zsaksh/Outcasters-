package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import android.content.Intent
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.backend.models.ModelManifest
import com.example.data.ChatMessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, sessionId: Long?, mode: String, targetLanguage: String) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.OutcastersApplication
    val chatDao = app.container.chatDao
    val inferenceEngine = app.container.inferenceEngine
    val modelManifestDao = app.container.modelManifestDao
    val allModels by modelManifestDao.getAllModels().collectAsState(initial = emptyList())
    val activeModel = allModels.find { it.activeStatus }
    val modelState by inferenceEngine.modelState.collectAsState()

    
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var currentSessionId by remember { mutableStateOf<Long?>(if (sessionId == -1L) null else sessionId) }
    var messages by remember { mutableStateOf<List<ChatMessageEntity>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var currentMode by remember { mutableStateOf(mode) }
    var currentTargetLanguage by remember { mutableStateOf(targetLanguage) }
    var isGenerating by remember { mutableStateOf(false) }
    var streamingResponse by remember { mutableStateOf("") }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scannedTextState = savedStateHandle?.getStateFlow("scanned_text", "")?.collectAsState()
    val scannedText = scannedTextState?.value ?: ""

    LaunchedEffect(scannedText) {
        if (scannedText.isNotBlank()) {
            inputText += (if (inputText.isEmpty()) "" else "\n") + scannedText
            savedStateHandle?.remove<String>("scanned_text")
        }
    }

    LaunchedEffect(currentSessionId) {
        currentSessionId?.let { sId ->
            chatDao.getMessagesForSession(sId).collectLatest { msgs ->
                messages = msgs
                if (msgs.isNotEmpty()) {
                    listState.animateScrollToItem(msgs.size - 1)
                }
            }
        }
    }

    LaunchedEffect(messages.size, streamingResponse) {
        if (messages.isNotEmpty() || streamingResponse.isNotEmpty()) {
            val target = messages.size + (if (isGenerating) 1 else 0)
            if (target > 0) {
                listState.scrollToItem(target - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentMode.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val modelName = activeModel?.displayName ?: "No Model"
                        val stateText = when (modelState) {
                            is com.example.backend.models.ModelState.Active -> "using $modelName (${currentTargetLanguage})"
                            is com.example.backend.models.ModelState.Loading -> "Loading $modelName..."
                            is com.example.backend.models.ModelState.Failed -> "Model Failed to Load"
                            else -> "No Model Loaded"
                        }
                        Text(
                            text = stateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (modelState is com.example.backend.models.ModelState.Failed) Color.Red else TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Chat", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    currentSessionId?.let { sId ->
                                        coroutineScope.launch {
                                            chatDao.deleteSession(sId)
                                            navController.navigateUp()
                                        }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status banner for offline/model state
            if (activeModel == null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("No active model. Go to Models to download one.", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty() && !isGenerating) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Start a conversation",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mode: ${currentMode.replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                    if (isGenerating) {
                        item {
                            if (streamingResponse.isEmpty()) {
                                ThinkingAnimation()
                            } else {
                                ChatBubble(
                                    ChatMessageEntity(
                                        sessionId = 0,
                                        role = "model",
                                        content = streamingResponse,
                                        timestamp = 0
                                    )
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Input Area
            Surface(
                color = SurfaceDark,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(
                        onClick = { navController.navigate("scan") },
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Attach", tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask something...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (inputText.isNotBlank() && !isGenerating) {
                        val canSend = modelState is com.example.backend.models.ModelState.Active
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (canSend) AccentTeal else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = canSend) {
                                    val text = inputText
                                    inputText = ""
                                    val currentMessagesSnapshot = messages.toList()
                                    val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
                                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                            isGenerating = false
                                            streamingResponse = ""
                                            currentSessionId?.let { sId ->
                                                chatDao.insertMessage(
                                                    ChatMessageEntity(sessionId = sId, role = "model", content = "Error: Inference failed safely. ${throwable.localizedMessage}", timestamp = System.currentTimeMillis())
                                                )
                                            }
                                        }
                                    }
                                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
                                        var sId = currentSessionId
                                        if (sId == null) {
                                            sId = chatDao.insertSession(
                                                com.example.data.ChatSessionEntity(
                                                    title = if (text.length > 20) text.take(20) + "..." else text,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            )
                                            currentSessionId = sId
                                        }
                                        
                                        chatDao.insertMessage(
                                            ChatMessageEntity(sessionId = sId, role = "user", content = text, timestamp = System.currentTimeMillis())
                                        )
                                        
                                        try {
                                            isGenerating = true
                                            streamingResponse = ""
                                            val postProcessor = com.example.backend.inference.PostProcessor()
                                            val history = currentMessagesSnapshot.map { com.example.backend.inference.ChatMessage(role = it.role, content = it.content, mode = currentMode, model = activeModel?.modelId ?: "", language = currentTargetLanguage) }
                                            val manifest = activeModel ?: com.example.backend.models.ModelManifest(modelId = "dummy", displayName = "Dummy", sourceUrl = "", fileName = "", chatTemplate = "fallback")
                                            val scannedTextFromState = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scanned_text") ?: ""
                                            val enhancedTask = text + if (scannedTextFromState.isNotBlank()) "\n[Context]: $scannedTextFromState" else ""
                                            
                                            // Initialize engine if needed (we rely on it being loaded globally)
                                            val responseFlow = inferenceEngine.generate(
                                                newTask = enhancedTask,
                                                history = history,
                                                mode = currentMode,
                                                targetLanguage = currentTargetLanguage,
                                                manifest = manifest,
                                                jobId = sId.toString()
                                            )
                                            var fullResponse = ""
                                            var lastUpdateTime = System.currentTimeMillis()
                                            
                                            responseFlow.collect { chunk -> 
                                                fullResponse += chunk 
                                                // Throttle UI updates to roughly every 50ms to reduce perceived latency and main thread locking
                                                val now = System.currentTimeMillis()
                                                if (now - lastUpdateTime > 50) {
                                                    streamingResponse = postProcessor.cleanResponse(fullResponse)
                                                    lastUpdateTime = now
                                                }
                                            }
                                            // Final flush
                                            val finalCleaned = postProcessor.cleanResponse(fullResponse)
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = finalCleaned, timestamp = System.currentTimeMillis())
                                            )
                                        } catch (e: Exception) {
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = "Error: Local model is not loaded.", timestamp = System.currentTimeMillis())
                                            )
                                        } finally {
                                            isGenerating = false
                                            streamingResponse = ""
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { navController.navigate("scan") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column {
            Surface(
                color = if (isUser) AccentTeal else SurfaceDark,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                shadowElevation = if (isUser) 0.dp else 2.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = if (isUser) Color.White else TextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
                IconButton(onClick = {
                    clipboardManager.setText(buildAnnotatedString { append(message.content) })
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun ThinkingAnimation() {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(AccentPurple.copy(alpha = alpha)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(AccentPurple.copy(alpha = alpha)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(AccentPurple.copy(alpha = alpha)))
    }
}
