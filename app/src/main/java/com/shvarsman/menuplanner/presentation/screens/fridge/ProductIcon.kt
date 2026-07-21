package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon

@Composable
fun ProductIcon(product: Product, modifier: Modifier = Modifier.size(40.dp)) {
    if (product.iconKey != Product.DEFAULT_ICON_KEY) {
        AsyncImage(
            model = "file:///android_asset/product_icons/${product.iconKey}.svg",
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