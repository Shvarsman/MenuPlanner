package com.shvarsman.menuplanner.presentation.ui.icons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.shvarsman.menuplanner.domain.model.Product
import kotlinx.coroutines.Dispatchers

@Composable
fun ProductIcon(product: Product, modifier: Modifier = Modifier.size(40.dp)) {
    if (product.iconKey != Product.DEFAULT_ICON_KEY) {
        val context = LocalContext.current
        val request = remember(product.iconKey, context) {
            ImageRequest.Builder(context)
                .data("file:///android_asset/product_icons/${product.iconKey}.svg")
                .decoderCoroutineContext(Dispatchers.Default)
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = null,
            modifier = modifier.fillMaxSize()
        )
    } else {
        CategoryIcon(
            category = product.category,
            modifier = modifier.fillMaxSize()
        )
    }
}