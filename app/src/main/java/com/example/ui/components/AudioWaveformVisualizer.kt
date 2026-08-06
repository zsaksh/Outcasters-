package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AiVoiceState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@Composable
fun AudioWaveformVisualizer(state: AiVoiceState) {
    val barCount = 5
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = Modifier.height(60.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val isAnimated = state == AiVoiceState.SPEAKING || state == AiVoiceState.LISTENING
            val targetHeight = if (isAnimated) {
                if (state == AiVoiceState.SPEAKING) 40f else 20f
            } else {
                8f
            }

            val heightMultiplier by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400 + (i * 100), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_height"
            )

            val color = when (state) {
                AiVoiceState.IDLE -> Color.Gray
                AiVoiceState.LISTENING -> Color(0xFF2196F3) // Blue
                AiVoiceState.PROCESSING -> Color(0xFF9C27B0) // Purple
                AiVoiceState.SPEAKING -> Color(0xFFFF9800) // Orange
            }
            
            // For processing, create a pulsing effect
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_alpha"
            )
            
            val finalAlpha = if (state == AiVoiceState.PROCESSING) alpha else 1.0f
            val finalHeight = if (isAnimated) (targetHeight * heightMultiplier).dp else 8.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(8.dp)
                    .height(finalHeight)
                    .clip(CircleShape)
                    .background(color.copy(alpha = finalAlpha))
            )
        }
    }
}
