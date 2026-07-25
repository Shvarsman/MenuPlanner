package com.shvarsman.menuplanner.presentation.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val UNDO_WINDOW_MS = 4000L

/**
 * Мгновенно помечает элемент как "удаляемый" (id попадает в pendingIds — список
 * должен отфильтровать такие id сразу), а реальное удаление откладывает на
 * UNDO_WINDOW_MS. Если за это время вызвать undo(id) — элемент возвращается,
 * реальное удаление из БД не происходит.
 */
class PendingDeleteManager<Id>(private val scope: CoroutineScope) {
    private val _pendingIds = MutableStateFlow<Set<Id>>(emptySet())
    val pendingIds: StateFlow<Set<Id>> = _pendingIds

    private val jobs = mutableMapOf<Id, Job>()

    fun requestDelete(id: Id, onConfirmedDelete: suspend () -> Unit) {
        _pendingIds.value += id
        jobs[id] = scope.launch {
            delay(UNDO_WINDOW_MS.milliseconds)
            _pendingIds.value -= id
            jobs.remove(id)
            onConfirmedDelete()
        }
    }

    fun undo(id: Id) {
        jobs.remove(id)?.cancel()
        _pendingIds.value -= id
    }
}

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