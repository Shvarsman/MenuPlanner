package com.shvarsman.menuplanner.presentation.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * onRequestDelete прячет элемент мгновенно (PendingDeleteManager.requestDelete
 * в ViewModel), затем показывается Snackbar. "Отменить" -> onUndo возвращает
 * элемент; иначе он реально удалится по таймеру внутри PendingDeleteManager,
 * независимо от того, сколько провисит сам Snackbar.
 */
@Composable
fun <T, Id> rememberOptimisticDelete(
    snackbarHostState: SnackbarHostState,
    idOf: (T) -> Id,
    message: (T) -> String,
    onRequestDelete: (Id) -> Unit,
    onUndo: (Id) -> Unit
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    return { item ->
        val id = idOf(item)
        onRequestDelete(id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message(item),
                actionLabel = "Отменить",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onUndo(id)
        }
    }
}