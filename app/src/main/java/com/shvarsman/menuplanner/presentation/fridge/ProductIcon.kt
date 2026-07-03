package com.shvarsman.menuplanner.presentation.fridge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Набор предустановленных иконок продуктов. iconKey хранится как строка в БД,
 * что позволяет не сериализовывать ImageVector напрямую.
 */
val productIconOptions: Map<String, ImageVector> = mapOf(
    "vegetable" to Icons.Filled.Eco,
    "meat" to Icons.Filled.LunchDining,
    "dairy" to Icons.Filled.Icecream,
    "bakery" to Icons.Filled.BakeryDining,
    "drink" to Icons.Filled.LocalCafe,
    "fruit" to Icons.Filled.Spa,
    "spice" to Icons.Filled.Grain,
    "other" to Icons.Filled.Inventory2
)

@Composable
fun ProductIcon(
    modifier: Modifier = Modifier,
    iconKey: String
) {
    val icon = productIconOptions[iconKey] ?: Icons.Filled.Inventory2
    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
