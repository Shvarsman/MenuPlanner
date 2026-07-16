package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Product

/**
 * Placeholder product icon. Replace with iconKey mapping when custom icons are added.
 */
@Composable
fun ProductIcon(product: Product, modifier: Modifier = Modifier.size(40.dp)) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Fastfood,
            contentDescription = null,
            modifier = Modifier.padding(8.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}