package com.shvarsman.menuplanner.presentation.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun Modifier.gradientStyle(
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundAlphaStart: Float = 0.05f,
    backgroundAlphaEnd: Float = 0.02f,
    borderAlpha: Float = 0.2f
): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = backgroundAlphaStart),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = backgroundAlphaEnd)
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = borderAlpha),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = borderAlpha)
            )
        ),
        shape = shape
    )