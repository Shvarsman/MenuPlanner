package com.shvarsman.coolinar.presentation.screens.cooking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.IngredientListCard
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotPose
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val PhotoHeight = 260.dp
private val ContentOverlap = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: CookingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.dishes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.recipe_not_found))
        }
        return
    }

    val dishes = state.dishes
    val pagerState = rememberPagerState(pageCount = { dishes.size })
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val maxOffsetPx = with(density) { (PhotoHeight - ContentOverlap).toPx() }
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minOffsetPx = with(density) { (statusBarHeightDp + 56.dp).toPx() }
    var offsetPx by remember { mutableFloatStateOf(maxOffsetPx) }

    val collapseConnection = remember(minOffsetPx, maxOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                return if (delta < 0) {
                    val newOffset = (offsetPx + delta).coerceIn(minOffsetPx, maxOffsetPx)
                    val consumedByHeader = newOffset - offsetPx
                    offsetPx = newOffset
                    Offset(0f, consumedByHeader)
                } else Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta > 0) {
                    val newOffset = (offsetPx + delta).coerceIn(minOffsetPx, maxOffsetPx)
                    val consumedByHeader = newOffset - offsetPx
                    offsetPx = newOffset
                    Offset(0f, consumedByHeader)
                } else Offset.Zero
            }
        }
    }

    val currentDish = dishes[pagerState.currentPage]

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column {
                    if (state.allDone) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MascotImage(
                                pose = MascotPose.EXCITED,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.cooking_all_dishes_done),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.markDishDone(currentDish.recipe) },
                        enabled = !currentDish.isDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (currentDish.isDone) {
                            MascotImage(
                                pose = MascotPose.HAPPY,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.done))
                        } else {
                            Text(stringResource(R.string.mark_dish_done))
                        }
                    }
                    Button(
                        onClick = onFinished,
                        enabled = state.allDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.done))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .nestedScroll(collapseConnection)
        ) {
            if (currentDish.recipe.photoUri != null) {
                AsyncImage(
                    model = rememberSizedImageRequest(
                        currentDish.recipe.photoUri,
                        480.dp,
                        PhotoHeight
                    ),
                    contentDescription = currentDish.recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PhotoHeight)
                        .align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PhotoHeight)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PhotoHeight)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                            endY = with(density) { 140.dp.toPx() }
                        )
                    )
            )

            Surface(
                shape = RoundedCornerShape(topStart = ContentOverlap, topEnd = ContentOverlap),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetPx.roundToInt()) }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (dishes.size == 1) {
                            currentDish.recipe.title
                        } else {
                            stringResource(R.string.cooking_dishes_count, dishes.size)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 12.dp,
                            end = 16.dp
                        )
                    )

                    if (dishes.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = MaterialTheme.colorScheme.background,
                            edgePadding = 16.dp
                        ) {
                            dishes.forEachIndexed { index, dish ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (dish.isDone) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                            }
                                            Text(dish.recipe.title, maxLines = 1)
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                        DishContent(dish = dishes[page], bottomContentPadding = padding.calculateBottomPadding())
                    }
                }
            }

            IconButton(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .statusBarsPadding()
                    .clip(CornerShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        CornerShape
                    ),
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    }
}

@Composable
private fun DishContent(
    dish: CookingDishUiState,
    bottomContentPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = bottomContentPadding + 16.dp)
    ) {
        if (dish.menuEntryIds.size > 1) {
            item {
                Text(
                    text = stringResource(R.string.cooking_portions_count, dish.menuEntryIds.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.ingredients),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        item {
            val scaledIngredients = dish.recipe.ingredients.map { ingredient ->
                ingredient.copy(quantity = ingredient.quantity * dish.menuEntryIds.size)
            }
            IngredientListCard(
                ingredients = scaledIngredients,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Text(
                text = stringResource(R.string.cooking_steps),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        CookingStepsReadOnly(steps = dish.recipe.steps)
    }
}