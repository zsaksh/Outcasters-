package com.example

import androidx.compose.runtime.*
import androidx.navigation.NavController

@Composable
fun Test(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scannedTextState = savedStateHandle?.getStateFlow("scanned_text", "")?.collectAsState()
    val scannedText = scannedTextState?.value ?: ""
    
    LaunchedEffect(scannedText) {
        if (scannedText.isNotBlank()) {
            inputText += (if (inputText.isEmpty()) "" else "\n") + scannedText
            savedStateHandle?.remove<String>("scanned_text")
        }
    }
}
