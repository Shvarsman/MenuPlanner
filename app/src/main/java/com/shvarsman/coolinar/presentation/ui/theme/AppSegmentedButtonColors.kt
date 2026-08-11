package com.shvarsman.coolinar.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable

/** Единый стиль SegmentedButton для всего приложения — чтобы не дублировать
 * цвета в каждом месте, где используется SingleChoiceSegmentedButtonRow. */
@Composable
fun appSegmentedButtonColors(): SegmentedButtonColors = SegmentedButtonDefaults.colors(
    activeContainerColor = MaterialTheme.colorScheme.primary,
    activeContentColor = MaterialTheme.colorScheme.onPrimary,
    activeBorderColor = MaterialTheme.colorScheme.primary,
    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
    inactiveContainerColor = MaterialTheme.colorScheme.surface,
    inactiveBorderColor = MaterialTheme.colorScheme.surface
)