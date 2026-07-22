package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Поле поиска в стиле M3 SearchBar ("таблетка" — surfaceContainerHigh,
 * скруглённые углы), встраиваемое прямо в title-слот TopAppBar. Отдельная
 * обёртка нужна, потому что SearchBarDefaults.InputField сам по себе не
 * рисует фон-контейнер — этот фон обычно даёт обёртывающий SearchBar/
 * DockedSearchBar, которого здесь нет (он не предназначен для вложения в TopAppBar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = SearchBarDefaults.colors().containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}