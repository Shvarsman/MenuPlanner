package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.background
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle

/**
 * Единый стиль "стеклянной" круглой кнопки для топ-баров: полупрозрачный
 * surfaceVariant + градиентная обводка (gradientStyle) + CornerShape —
 * тот же визуальный язык, что у плавающего нижнего навбара и GlassFab.
 * Используется как navigationIcon/actions в TopAppBar на всех экранах.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CornerShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                CornerShape
            )
            .gradientStyle(shape = CornerShape)
    ) {
        content()
    }
}