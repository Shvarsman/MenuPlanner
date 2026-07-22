package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * LargeTopAppBar, где заголовок занимает до 2 строк в развёрнутом состоянии
 * (снизу, крупным шрифтом) и схлопывается ровно в одну строку с многоточием
 * при скролле — collapsedFraction из scrollBehavior.state показывает, насколько
 * панель уже сжата, и maxLines переключается по этому значению.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingLargeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    LargeTopAppBar(
        title = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction
            Text(
                text = title,
                maxLines = if (collapsedFraction > 0.5f) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior
    )
}