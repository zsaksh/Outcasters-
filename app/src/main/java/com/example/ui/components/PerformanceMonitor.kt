package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.clickable
import com.example.backend.device.PerformanceLogger
import kotlinx.coroutines.delay

@Composable
fun PerformanceMonitor(
    modifier: Modifier = Modifier,
    isGenerating: Boolean
) {
    val context = LocalContext.current
    var tokensPerSecHistory by remember { mutableStateOf(List(20) { 0f }) }
    var memoryPressureHistory by remember { mutableStateOf(List(20) { 0f }) }
    
    var currentTps by remember { mutableFloatStateOf(0f) }
    var currentMem by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(isGenerating) {
        while (true) {
            if (isGenerating) {
                currentTps = 15f + (Math.random() * 5).toFloat() // Mock 15-20 TPS
                currentMem = 60f + (Math.random() * 10).toFloat() // Mock 60-70% Memory
            } else {
                currentTps = 0f
                currentMem = 50f + (Math.random() * 5).toFloat() // Mock idle memory
            }
            
            tokensPerSecHistory = (tokensPerSecHistory.drop(1) + currentTps)
            memoryPressureHistory = (memoryPressureHistory.drop(1) + currentMem)
            
            PerformanceLogger.logStat(currentTps, currentMem)
            
            delay(500)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            PerformanceLogger.exportToJson(context)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Inference Speed", color = Color.White, fontSize = 10.sp)
                    Text(String.format("%.1f t/s", currentTps), color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                IconButton(onClick = { PerformanceLogger.exportToJson(context) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Save, contentDescription = "Export Logs", tint = Color.White)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Memory Pressure", color = Color.White, fontSize = 10.sp)
                    Text(String.format("%.1f %%", currentMem), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (tokensPerSecHistory.size - 1)
                
                // Draw TPS line (Green)
                val tpsPath = Path()
                val maxTps = 40f
                tokensPerSecHistory.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value / maxTps) * height).coerceIn(0f, height)
                    if (index == 0) tpsPath.moveTo(x, y) else tpsPath.lineTo(x, y)
                }
                drawPath(
                    path = tpsPath,
                    color = Color(0xFF00E676),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                // Draw Mem line (Red)
                val memPath = Path()
                val maxMem = 100f
                memoryPressureHistory.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value / maxMem) * height).coerceIn(0f, height)
                    if (index == 0) memPath.moveTo(x, y) else memPath.lineTo(x, y)
                }
                drawPath(
                    path = memPath,
                    color = Color(0xFFFF5252),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}
