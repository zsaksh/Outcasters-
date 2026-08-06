package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class ChartData(val color: Color, val points: List<Float>)

@Composable
fun PerformanceChart(
    modifier: Modifier = Modifier,
    chartDataList: List<ChartData>
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        
        // Draw grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height - (i * height / gridLines)
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Max possible value is 100 for these scores
        val maxValue = 100f
        
        chartDataList.forEach { chartData ->
            if (chartData.points.isEmpty()) return@forEach
            
            val path = Path()
            val pointsCount = chartData.points.size
            
            val stepX = if (pointsCount > 1) width / (pointsCount - 1) else width
            
            chartData.points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value / maxValue) * height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = chartData.color,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            
            // Draw points
            chartData.points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value / maxValue) * height)
                drawCircle(
                    color = chartData.color,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
