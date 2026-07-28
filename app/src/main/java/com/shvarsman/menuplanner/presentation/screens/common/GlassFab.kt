package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.presentation.ui.theme.CornerShape
import com.shvarsman.menuplanner.presentation.ui.theme.gradientStyle

/** FAB в общем "стеклянном" стиле панели навигации/меню — та же заливка
 * surfaceVariant(0.9f) + gradientStyle(), но в форме и размере обычного FAB. */
@Composable
fun GlassFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CornerShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        modifier = modifier
            .size(56.dp)
            .gradientStyle(shape = CornerShape)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { content() }
    }
}