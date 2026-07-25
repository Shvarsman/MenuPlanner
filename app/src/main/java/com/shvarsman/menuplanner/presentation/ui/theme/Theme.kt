package com.shvarsman.menuplanner.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val lightScheme = lightColorScheme(
    primary = Green,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surfaceContainer = White,
    onSurfaceVariant = OnSurfaceContainerLight,
    surface = GrayTransparentLight,
    onSurface = OnSurfaceLight
)

private val darkScheme = darkColorScheme(
    primary = Green,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceContainerDark,
    surface = GrayTransparentDark,
    onSurface = OnSurfaceDark
)

val AppCornerRadius = 28.dp

@Composable
fun MenuPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}