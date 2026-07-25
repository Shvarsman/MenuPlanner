package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil3.request.ImageRequest

/** Builds an ImageRequest that decodes only up to the on-screen pixel size. */
@Composable
fun rememberSizedImageRequest(model: Any?, width: Dp, height: Dp): ImageRequest {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(model, width, height, density) {
        val widthPx = with(density) { width.roundToPx() }.coerceAtLeast(1)
        val heightPx = with(density) { height.roundToPx() }.coerceAtLeast(1)
        ImageRequest.Builder(context)
            .data(model)
            .size(widthPx, heightPx)
            .build()
    }
}
