package com.shvarsman.menuplanner.presentation.screens.cooking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest

/** Displays recipe steps read-only — no TextField, for the Cooking screen. */
fun LazyListScope.CookingStepsReadOnly(steps: List<StepContentItem>) {
    var stepNum = 0
    val stepNumbers = steps.map { item ->
        if (item is StepContentItem.Text && item.content.isNotBlank()) ++stepNum else 0
    }

    steps.forEachIndexed { index, item ->
        item(key = "cook_step_$index") {
            when (item) {
                is StepContentItem.Image -> {
                    val isAlreadyRendered = index > 0 && steps[index - 1] is StepContentItem.Image
                    if (!isAlreadyRendered) {
                        val groupUrls = steps.drop(index).takeWhile { it is StepContentItem.Image }
                            .map { (it as StepContentItem.Image).url }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupUrls.forEach { url ->
                                AsyncImage(
                                    model = rememberSizedImageRequest(url, 200.dp, 200.dp),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }

                is StepContentItem.Text -> {
                    if (item.content.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${stepNumbers[index]}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(item.content, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}