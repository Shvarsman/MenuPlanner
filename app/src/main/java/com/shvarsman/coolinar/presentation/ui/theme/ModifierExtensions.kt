package com.shvarsman.coolinar.presentation.ui.theme

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.gradientStyle(
    shape: Shape = CornerShape,
    backgroundAlphaStart: Float = 0.05f,
    backgroundAlphaEnd: Float = 0.02f,
    borderAlpha: Float = 0.15f
): Modifier = this
    .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = borderAlpha),
        shape = shape
    )
