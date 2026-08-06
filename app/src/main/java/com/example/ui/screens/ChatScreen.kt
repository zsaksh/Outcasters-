@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")
package com.example.ui.screens




import androidx.compose.animation.core.animateFloat




import androidx.compose.animation.core.infiniteRepeatable




import androidx.compose.animation.core.rememberInfiniteTransition




import androidx.compose.animation.core.tween




import androidx.compose.animation.core.LinearEasing




import androidx.compose.animation.core.RepeatMode






import android.content.Intent




import androidx.compose.foundation.background




import androidx.compose.foundation.clickable




import androidx.compose.foundation.layout.*




import androidx.compose.foundation.lazy.LazyColumn




import androidx.compose.foundation.lazy.items




import androidx.compose.foundation.shape.CircleShape




import androidx.compose.foundation.shape.RoundedCornerShape




import androidx.compose.material.icons.Icons




import androidx.compose.material.icons.automirrored.filled.ArrowBack




import androidx.compose.material.icons.automirrored.filled.Send




import androidx.compose.material.icons.filled.Add




import androidx.compose.material.icons.filled.ArrowDropDown




import androidx.compose.material.icons.filled.CameraAlt




import androidx.compose.material.icons.filled.Mic




import androidx.compose.material.icons.filled.Psychology




import androidx.compose.material.icons.filled.MoreVert




import androidx.compose.material3.*




import androidx.compose.runtime.*




import androidx.compose.ui.Alignment




import androidx.compose.ui.Modifier




import androidx.compose.ui.draw.clip




import androidx.compose.ui.graphics.Color




import androidx.compose.ui.platform.LocalContext




import androidx.compose.ui.text.font.FontWeight





import androidx.compose.ui.text.AnnotatedString




import androidx.compose.ui.text.SpanStyle




import androidx.compose.ui.text.buildAnnotatedString




import androidx.compose.ui.text.withStyle





import androidx.compose.ui.unit.dp




import androidx.compose.ui.unit.sp




import androidx.navigation.NavController




import com.example.OutcastersApplication




import com.example.backend.device.BatteryStateReceiver




import com.example.data.ChatMessageEntity




import com.example.data.ChatSessionEntity




import com.example.data.SrsDatabase




import com.example.ui.theme.*




import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)


fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        var isBold = false
        for (part in parts) {
            if (isBold) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
            isBold = !isBold
        }
    }
}




@Composable

fun ChatScreen(navController: NavController, sessionId: Long? = null, mode: String = "chat", targetLanguage: String = "French") {
    val context = LocalContext.current
    val app = context.applicationContext as OutcastersApplication
    val chatDao = app.container.chatDao
    val inferenceEngine = app.container.inferenceEngine
    val db = remember { androidx.room.Room.databaseBuilder(context, SrsDatabase::class.java, "srs_db").fallbackToDestructiveMigration().build() }
    
    val coroutineScope = rememberCoroutineScope()
    var currentSessionId by remember { mutableStateOf(sessionId) }
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
    
    val modelState by inferenceEngine.modelState.collectAsState()
    
    val allModels by db.modelManifestDao().getAllModels().collectAsState(initial = emptyList())
    val downloadedModels = allModels.filter { it.installStatus == "ready" }
    val activeModel = allModels.find { it.activeStatus }
    
    var showModelBottomSheet by remember { mutableStateOf(false) }
    var isLowPowerMode by remember { mutableStateOf(false) }
    
    val batteryReceiver = remember { BatteryStateReceiver(context) }
    val batteryPct by batteryReceiver.batteryLevel.collectAsState()
    val recommendLowPower by batteryReceiver.isLowPowerModeRecommended.collectAsState()
    
    DisposableEffect(Unit) {
        batteryReceiver.register()
        onDispose {
            batteryReceiver.unregister()
        }
    }
    
    LaunchedEffect(currentSessionId) {
        currentSessionId?.let { id ->
            chatDao.getMessagesForSession(id).collect {
                messages = it
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(activeModel?.displayName ?: (if (messages.isEmpty()) "New Chat" else "Chat"), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
                        
                        // Status Pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (modelState) {
                                            is com.example.backend.models.ModelState.Active -> AccentTeal
                                            is com.example.backend.models.ModelState.Loading -> AccentPurple
                                            is com.example.backend.models.ModelState.NotInstalled -> Color.Red
                                            else -> Color.Gray
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (modelState) {
                                    is com.example.backend.models.ModelState.Active -> "Ready \uD83DFE2" //🟢
                                    is com.example.backend.models.ModelState.Loading -> "Loading \uD83DFE1" //🟡
                                    is com.example.backend.models.ModelState.NotInstalled -> "Offline \u26AA" //⚪
                                    else -> "Download Required \uD83DFE1" //🔴
                                },
                                fontSize = 12.sp, color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showModelBottomSheet = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (currentMode == "language" || currentMode == "translate" || currentMode == "vocabulary" || currentMode == "grammar" || currentMode == "practice") {
                val languages = listOf("French", "Spanish", "Japanese", "German", "Mandarin")
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language Mode:", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(currentTargetLanguage)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        currentTargetLanguage = lang
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Tasks row
                val langTasks = listOf("Translate", "Vocabulary", "Grammar", "Practice")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    langTasks.forEach { task ->
                        val isSelected = currentMode == task.lowercase() || (currentMode == "language" && task == "Practice")
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentMode = task.lowercase() },
                            label = { Text(task) }
                        )
                    }
                }
                HorizontalDivider()
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            when (modelState) {
                                is com.example.backend.models.ModelState.Loading -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        ThinkingAnimation()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Warming up local model...", color = TextSecondary, fontSize = 14.sp)
                                    }
                                }
                                is com.example.backend.models.ModelState.NotInstalled -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(AccentTeal.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Psychology, "AI", tint = AccentTeal, modifier = Modifier.size(32.dp))
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("No Active Model", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("You need to activate a local model\nbefore starting a conversation.", fontSize = 14.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { navController.navigate("models") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                                        ) {
                                            Text("Go to Model Hub")
                                        }
                                    }
                                }
                                else -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(AccentTeal.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Psychology, "AI", tint = AccentTeal, modifier = Modifier.size(32.dp))
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("No conversations yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Start a new chat or scan\na question to begin.", fontSize = 14.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
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
            
            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isActive = modelState is com.example.backend.models.ModelState.Active
                    TextField(
                        value = inputText,
                        onValueChange = { if (isActive) inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        placeholder = { 
                            Text(
                                text = when (modelState) {
                                    is com.example.backend.models.ModelState.Loading -> "Loading your local model..."
                                    is com.example.backend.models.ModelState.NotInstalled -> "Choose a model to start chatting"
                                    else -> "Ask a question..."
                                },
                                color = TextSecondary 
                            ) 
                        },
                        enabled = isActive,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            disabledTextColor = TextSecondary
                        ),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Mic, "Mic", tint = if (isActive) TextSecondary else TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp).clickable(enabled = isActive) { })
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Filled.CameraAlt, "Scan", tint = if (isActive) TextSecondary else TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp).clickable(enabled = isActive) { navController.navigate("scan") })
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (inputText.isNotBlank() && isActive) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AccentTeal)
                                .clickable {
                                    val text = inputText
                                    inputText = ""
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
                                                ChatSessionEntity(
                                                    title = text.take(20) + "...",
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
                                            val promptRouter = com.example.backend.inference.PromptRouter()
                                            val postProcessor = com.example.backend.inference.PostProcessor()
                                            val history = messages.map { com.example.backend.inference.ChatMessage(it.role, it.content) }
                                            val manifest = activeModel ?: com.example.backend.models.ModelManifest(modelId = "dummy", displayName = "Dummy", sourceUrl = "", fileName = "", chatTemplate = "fallback")
                                            val scannedText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scanned_text") ?: ""
                                            val routedPrompt = promptRouter.route(
                                                mode = currentMode,
                                                targetLanguage = currentTargetLanguage,
                                                manifest = manifest,
                                                history = history,
                                                newTask = text,
                                                ocrContext = scannedText,
                                                retrievalContext = ""
                                            )
                                            val llamaBridge = com.example.inference.LlamaBridge()
                                            
                                            var retryCount = 0
                                            var initSuccess = false
                                            while (retryCount < 3 && !initSuccess) {
                                                try {
                                                    if (!llamaBridge.isModelReady()) {
                                                        val path = activeModel?.fileName ?: "default.gguf"
                                                        llamaBridge.loadModel(path)
                                                    }
                                                    initSuccess = llamaBridge.isModelReady()
                                                } catch (e: Exception) {
                                                    // Catch null pointer or initialization errors and retry
                                                }
                                                if (!initSuccess) {
                                                    retryCount++
                                                    kotlinx.coroutines.delay(100)
                                                }
                                            }
                                            
                                            if (!initSuccess) {
                                                throw IllegalStateException("Failed to initialize C++ inference engine after retries.")
                                            }
                                            
                                            llamaBridge.clearKvCache()
                                            
                                            val systemContext = "You are Outcasters AI, a private local study assistant."
                                            val formattedPrompt = com.example.inference.ChatTemplateFormatter.formatSmolLM2(
                                                messages = history.takeLast(2) + com.example.backend.inference.ChatMessage("user", text),
                                                systemPrompt = systemContext
                                            )
                                            
                                            val responseFlow = inferenceEngine.generate(formattedPrompt)
                                            var fullResponse = ""
                                            responseFlow.collect { word -> 
                                                fullResponse += word 
                                                streamingResponse = postProcessor.cleanResponse(fullResponse)
                                            }
                                            chatDao.insertMessage(
                                                ChatMessageEntity(sessionId = sId, role = "model", content = postProcessor.cleanResponse(fullResponse), timestamp = System.currentTimeMillis())
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
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        
        if (showModelBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showModelBottomSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Model", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (downloadedModels.isEmpty()) {
                        Text("No models downloaded.", color = TextSecondary)
                    } else {
                        downloadedModels.forEach { model ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            db.modelManifestDao().setActiveModelAtomic(model.modelId)
                                            showModelBottomSheet = false
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                RadioButton(
                                    selected = model.activeStatus,
                                    onClick = { 
                                        coroutineScope.launch {
                                            db.modelManifestDao().setActiveModelAtomic(model.modelId)
                                            showModelBottomSheet = false
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(model.displayName, fontSize = 16.sp, color = TextPrimary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Low Power Mode", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Reduces context window size to save energy.", fontSize = 12.sp, color = TextSecondary)
                            if (recommendLowPower) {
                                Text("Recommended (Battery at $batteryPct%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Switch(checked = isLowPowerMode, onCheckedChange = { 
                            isLowPowerMode = it
                            inferenceEngine.contextWindow = if (it) 1024 else 4096
                        })
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}




@Composable

fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    
    if (isUser) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(start = 48.dp, top = 8.dp, bottom = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(BubbleUser)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.Black
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Psychology,
                    contentDescription = "AI",
                    tint = AccentTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Outcasters AI",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AccentTeal
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.content,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = TextPrimary
            )
        }
    }
}




@Composable

fun ThinkingAnimation() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "thinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Icon(
            Icons.Filled.Psychology,
            contentDescription = "AI Thinking",
            tint = AccentTeal.copy(alpha = alpha),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Outcasters AI is thinking...",
            color = AccentTeal.copy(alpha = alpha),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


