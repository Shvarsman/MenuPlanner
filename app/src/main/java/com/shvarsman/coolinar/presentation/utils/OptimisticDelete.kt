package com.shvarsman.coolinar.presentation.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Удаление вызывается СРАЗУ и по-настоящему (onDelete), а не откладывается
 * по таймеру — благодаря этому оно не "отменяется само" из-за обрыва
 * корутины при уходе с экрана / уничтожении ViewModel. Кнопка "Отменить"
 * не блокирует удаление, а восстанавливает уже удалённый элемент (onUndo) —
 * это естественно ложится на существующий soft-delete (isDeleted/updatedAt).
 *
 * Снекбары не встают в очередь: перед показом нового текущий снекбар
 * принудительно скрывается (currentSnackbarData?.dismiss()), поэтому при
 * последовательном удалении нескольких элементов подряд пользователь видит
 * только последний снекбар с таймером, который каждый раз стартует заново,
 * а не накопленную очередь на много секунд/минут.
 */
@Composable
fun <T, Id> rememberOptimisticDelete(
    snackbarHostState: SnackbarHostState,
    idOf: (T) -> Id,
    message: (T) -> String,
    undoLabel: String,
    onDelete: (Id) -> Unit,
    onUndo: (Id) -> Unit
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    return { item ->
        val id = idOf(item)
        onDelete(id)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message(item),
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onUndo(id)
        }
    }
}