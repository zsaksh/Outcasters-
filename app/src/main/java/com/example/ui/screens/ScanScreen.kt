package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.OutcastersApplication
import com.example.data.ChatMessageEntity
import com.example.data.ChatSessionEntity
import com.example.data.OcrScanEntity
import com.example.ocr.OcrPipeline
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ScanScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreview(navController)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to scan documents.")
        }
    }
}

@Composable
fun CameraPreview(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val app = context.applicationContext as OutcastersApplication
    val ocrDao = app.container.ocrDao
    val chatDao = app.container.chatDao
    val ocrPipeline = remember { OcrPipeline(context) }
    
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = selectedUris + uris
    }
    
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUris = selectedUris + it }
    }
    
    var cropRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var startOffset by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    var currentOffset by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startOffset = offset
                            currentOffset = offset
                            cropRect = null
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentOffset = currentOffset?.plus(dragAmount)
                        },
                        onDragEnd = {
                            if (startOffset != null && currentOffset != null) {
                                cropRect = androidx.compose.ui.geometry.Rect(startOffset!!, currentOffset!!)
                            }
                        }
                    )
                }
        ) {
            val rect = cropRect ?: if (startOffset != null && currentOffset != null) androidx.compose.ui.geometry.Rect(startOffset!!, currentOffset!!) else null
            rect?.let { r ->
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    topLeft = r.topLeft,
                    size = r.size
                )
                drawRect(
                    color = Color.Yellow,
                    topLeft = r.topLeft,
                    size = r.size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Top Status Bar
        if (selectedUris.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedUris.size} item(s) selected", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
                            if (isProcessing) return@Button
                            isProcessing = true
                            coroutineScope.launch {
                                val combinedText = StringBuilder()
                                selectedUris.forEachIndexed { index, uri ->
                                    val type = context.contentResolver.getType(uri)
                                    val text = if (type?.contains("pdf") == true || uri.toString().endsWith(".pdf")) {
                                        ocrPipeline.extractTextFromPdf(uri)
                                    } else {
                                        ocrPipeline.extractTextFromUri(uri)
                                    }
                                    combinedText.append("--- Document ${index + 1} ---\n").append(text).append("\n\n")
                                }
                                
                                val finalText = combinedText.toString()
                                
                                val scanId = ocrDao.insertScan(
                                    OcrScanEntity(
                                        title = "Multi-Scan ${System.currentTimeMillis()}",
                                        extractedText = finalText,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                
                                isProcessing = false
                                val prevRoute = navController.previousBackStackEntry?.destination?.route
                                if (prevRoute?.startsWith("chat") == true) {
                                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_text", finalText)
                                    navController.popBackStack()
                                } else {
                                    val sessionId = chatDao.insertSession(
                                        ChatSessionEntity(
                                            title = "Context from Scans",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    
                                    chatDao.insertMessage(
                                        ChatMessageEntity(
                                            sessionId = sessionId,
                                            role = "user",
                                            content = "I selected documents with this text: \n$finalText\nWhat does this mean?",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    
                                    navController.navigate("chat/$sessionId?mode=scan_solve") {
                                        popUpTo("home")
                                    }
                                }
                            }
                        },
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = "Process")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Process")
                        }
                    }
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.Image, contentDescription = "Gallery")
            }

            FloatingActionButton(
                onClick = {
                    imageCapture?.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    val rotationDegrees = image.imageInfo.rotationDegrees
                                    val originalBitmap = image.toBitmap()
                                    
                                    val rotatedBitmap = if (rotationDegrees != 0) {
                                        val matrix = android.graphics.Matrix()
                                        matrix.postRotate(rotationDegrees.toFloat())
                                        android.graphics.Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                                    } else originalBitmap
                                    
                                    val finalBitmap = if (cropRect != null) {
                                        val leftPct = Math.max(0f, cropRect!!.left / screenWidth)
                                        val topPct = Math.max(0f, cropRect!!.top / screenHeight)
                                        val widthPct = Math.min(1f, cropRect!!.width / screenWidth)
                                        val heightPct = Math.min(1f, cropRect!!.height / screenHeight)
                                        
                                        val bLeft = (leftPct * rotatedBitmap.width).toInt().coerceAtLeast(0)
                                        val bTop = (topPct * rotatedBitmap.height).toInt().coerceAtLeast(0)
                                        val bWidth = (widthPct * rotatedBitmap.width).toInt().coerceAtMost(rotatedBitmap.width - bLeft)
                                        val bHeight = (heightPct * rotatedBitmap.height).toInt().coerceAtMost(rotatedBitmap.height - bTop)
                                        
                                        if (bWidth > 0 && bHeight > 0) {
                                            android.graphics.Bitmap.createBitmap(rotatedBitmap, bLeft, bTop, bWidth, bHeight)
                                        } else rotatedBitmap
                                    } else rotatedBitmap
                                    
                                    val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                                    file.outputStream().use { out ->
                                        finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                                    }
                                    selectedUris = selectedUris + Uri.fromFile(file)
                                    cropRect = null
                                    startOffset = null
                                    currentOffset = null
                                } catch (e: Exception) {
                                    Log.e("CameraPreview", "Error processing bitmap", e)
                                } finally {
                                    image.close()
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                            }
                        }
                    )
                },
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(32.dp), tint = Color.White)
            }

            IconButton(
                onClick = { pdfLauncher.launch("application/pdf") },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF")
            }
        }
    }
}
