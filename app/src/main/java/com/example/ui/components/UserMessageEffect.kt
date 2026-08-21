package com.example.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun UserMessageEffect(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onMessageConsumed: () -> Unit
) {
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
            onMessageConsumed()
        }
    }
}