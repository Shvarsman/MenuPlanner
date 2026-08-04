package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFilterChip(
    displayText: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    maxMenuHeight: Dp = 300.dp,
    shape: Shape = RoundedCornerShape(28.dp),
    menuContent: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = isActive,
            onClick = { expanded = true },
            label = { Text(displayText) },
            leadingIcon = if (isActive) {
                {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null,
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            shape = shape,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.surface,
                selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface
            )
        )
        DropdownMenu(
            modifier = modifier
                .clip(RoundedCornerShape(28.dp))
                .heightIn(max = maxMenuHeight)
                .gradientStyle(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 0.dp
        ) {
            menuContent { expanded = false }
        }
    }
}