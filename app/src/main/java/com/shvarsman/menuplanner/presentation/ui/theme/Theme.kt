package com.shvarsman.menuplanner.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val lightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    tertiary = Tertiary,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surfaceContainer = White,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceContainerLight,
    surface = GrayTransparentLight,
    onSurface = Black,
    error = ErrorLight,
    onError = OnError
)

private val darkScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    tertiary = Tertiary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceContainerDark,
    surface = GrayTransparentDark,
    onSurface = White,
    error = ErrorDark,
    onError = OnError
)

val CornerShape = RoundedCornerShape(28.dp)
val FloatingBottomBarClearance = 96.dp

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