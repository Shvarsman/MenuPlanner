package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape

/**
 * Единый стиль снекбара для всего приложения — своя форма (CornerShape) и
 * цвета из темы вместо дефолтного Material inverse-surface. Подключается
 * в Scaffold(snackbarHost = { AppSnackbarHost(hostState) }) вместо
 * стандартного SnackbarHost(hostState).
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            snackbarData = data,
            shape = CornerShape,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            actionColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
    }
}