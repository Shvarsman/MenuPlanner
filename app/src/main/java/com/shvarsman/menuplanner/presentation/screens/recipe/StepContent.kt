package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import kotlinx.coroutines.launch

sealed interface RenderedStep {
    data class Text(
        val originalIndex: Int,
        val stepNumber: Int,
        val item: StepContentItem.Text
    ) : RenderedStep

    data class ImageGroup(
        val startIndex: Int,
        val urls: List<String>
    ) : RenderedStep
}

fun buildRenderedSteps(steps: List<StepContentItem>): List<RenderedStep> {
    val result = mutableListOf<RenderedStep>()
    var stepNum = 0
    var i = 0
    while (i < steps.size) {
        when (val item = steps[i]) {
            is StepContentItem.Text -> {
                stepNum++
                result.add(RenderedStep.Text(i, stepNum, item))
                i++
            }
            is StepContentItem.Image -> {
                val imageUrls = mutableListOf<String>()
                val startIndex = i
                while (i < steps.size && steps[i] is StepContentItem.Image) {
                    imageUrls.add((steps[i] as StepContentItem.Image).url)
                    i++
                }
                result.add(RenderedStep.ImageGroup(startIndex, imageUrls))
            }
        }
    }
    return result
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.StepContent(
    renderedSteps: List<RenderedStep>, // ИСПРАВЛЕНО: Принимаем уже готовый и кэшированный список
    focusRequestIndex: Int?,
    onDeleteImageClick: (index: Int) -> Unit,
    onTextChange: (index: Int, text: String) -> Unit,
    onNext: (currentIndex: Int) -> Unit,
    onFocusConsumed: () -> Unit
) {
    renderedSteps.forEach { step ->
        when (step) {
            is RenderedStep.ImageGroup -> {
                item(key = "images_${step.startIndex}") {
                    StepImageGroup(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        imageUrls = step.urls,
                        onDeleteImageClick = { imageIndex ->
                            onDeleteImageClick(step.startIndex + imageIndex)
                        }
                    )
                }
            }

            is RenderedStep.Text -> {
                item(key = "text_${step.originalIndex}") {
                    StepTextBlock(
                        stepNumber = step.stepNumber,
                        text = step.item.content,
                        shouldRequestFocus = focusRequestIndex == step.originalIndex,
                        onTextChange = { onTextChange(step.originalIndex, it) },
                        onNext = { onNext(step.originalIndex) },
                        onFocusConsumed = onFocusConsumed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StepTextBlock(
    stepNumber: Int,
    text: String,
    shouldRequestFocus: Boolean,
    onTextChange: (String) -> Unit,
    onNext: () -> Unit,
    onFocusConsumed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            bringIntoViewRequester.bringIntoView()
            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 16.dp, start = 8.dp, end = 4.dp)
                .size(24.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        TextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                },
            value = text,
            onValueChange = onTextChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = {
                Text(
                    text = "Опишите шаг...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() })
        )
    }
}

@Composable
private fun StepImageGroup(
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    onDeleteImageClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUrls.forEachIndexed { index, url ->
            StepImageContent(
                modifier = Modifier.weight(1f),
                imageUrl = url,
                onDeleteClick = { onDeleteImageClick(index) }
            )
        }
    }
}

@Composable
private fun StepImageContent(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onDeleteClick: () -> Unit
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = rememberSizedImageRequest(imageUrl, 400.dp, 240.dp),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCornerRadius))
        )
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить фото",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}