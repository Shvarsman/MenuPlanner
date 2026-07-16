package com.shvarsman.menuplanner.presentation.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholderText: String,
    searchQuery: String,
    onQueryChanged: (String) -> Unit
) {
    val (localQuery, onLocalQueryChange) = rememberDebouncedSearch(searchQuery, onQueryChanged)

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            ),
        value = localQuery,
        onValueChange = onLocalQueryChange,
        placeholder = {
            Text(placeholderText)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Поиск",
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
    )
}
