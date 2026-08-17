package com.shvarsman.coolinar.presentation.tour

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.navigation.Destination
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
private fun Modifier.blockAllInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

private fun messageFor(step: TourStep) = when (step) {
    TourStep.HOME -> R.string.tour_home
    TourStep.FRIDGE -> R.string.tour_fridge
    TourStep.SHOPPING_LIST -> R.string.tour_shoppinglist
    TourStep.RECIPES -> R.string.tour_recipes
    TourStep.WEEK_MENU -> R.string.tour_weekmenu
    TourStep.PROFILE -> R.string.tour_profile
}

@Composable
fun TourOverlay(
    tourViewModel: TourViewModel,
    rootNavController: NavHostController
) {
    val step by tourViewModel.currentStep.collectAsStateWithLifecycle()
    val currentStep = step ?: return

    var mascotVisible by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(currentStep) {
        mascotVisible = true
    }

    fun onNext() {
        if (isTransitioning) return
        isTransitioning = true
        mascotVisible = false
    }

    LaunchedEffect(isTransitioning) {
        if (!isTransitioning) return@LaunchedEffect
        delay(250.milliseconds)

        when (currentStep) {
            TourStep.HOME -> tourViewModel.requestTabSelection(Destination.Fridge.route)
            TourStep.FRIDGE -> rootNavController.navigate(Destination.ShoppingList.route)
            TourStep.SHOPPING_LIST -> {
                tourViewModel.requestTabSelection(Destination.Recipes.route)
                rootNavController.popBackStack()
            }
            TourStep.RECIPES -> rootNavController.navigate(Destination.WeekMenu.route)
            TourStep.WEEK_MENU -> {
                tourViewModel.requestTabSelection(Destination.Profile.route)
                rootNavController.popBackStack()
            }
            TourStep.PROFILE -> {
                tourViewModel.finish()
                isTransitioning = false
                return@LaunchedEffect
            }
        }

        delay(350.milliseconds)
        tourViewModel.next()
        isTransitioning = false
    }

    Box(Modifier
        .fillMaxSize()
        .blockAllInput())

    MascotTourTip(
        visible = mascotVisible,
        message = stringResource(messageFor(currentStep)),
        isLastStep = currentStep == TourStep.PROFILE,
        onNext = { onNext() }
    )
}