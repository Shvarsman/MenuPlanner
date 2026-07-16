package com.shvarsman.menuplanner.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private const val SEARCH_DEBOUNCE_MS = 300L

fun <T> Flow<T>.debounceSearch(): Flow<T> =
    debounce(SEARCH_DEBOUNCE_MS).distinctUntilChanged()

fun <T, R> Flow<T>.mapOnDefault(transform: suspend (T) -> R): Flow<R> =
    map(transform).flowOn(Dispatchers.Default)

/**
 * Local search field state that updates immediately in the UI but debounces
 * callbacks to the ViewModel, avoiding list recomposition on every keystroke.
 */
@Composable
fun rememberDebouncedSearch(
    externalQuery: String,
    onQueryChanged: (String) -> Unit
): Pair<String, (String) -> Unit> {
    var localQuery by remember { mutableStateOf(externalQuery) }

    LaunchedEffect(externalQuery) {
        if (externalQuery != localQuery) {
            localQuery = externalQuery
        }
    }

    LaunchedEffect(localQuery) {
        snapshotFlow { localQuery }
            .debounceSearch()
            .collect { query ->
                if (query != externalQuery) {
                    onQueryChanged(query)
                }
            }
    }

    return localQuery to { localQuery = it }
}
