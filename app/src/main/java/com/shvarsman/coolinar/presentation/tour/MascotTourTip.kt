package com.shvarsman.coolinar.presentation.tour

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotPose
import com.shvarsman.coolinar.presentation.screens.common.MascotSpeechBubble
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Шаг guided-тура: облако мыслей с текстом + кнопка "Далее"/"Закончить".
 * Popup остаётся смонтированным чуть дольше, чем visible = false, чтобы
 * exit-анимация (slide + fade + лёгкий scale) успела доиграть, а не
 * обрывалась мгновенным размонтированием.
 */
@Composable
fun MascotTourTip(
    visible: Boolean,
    message: String,
    isLastStep: Boolean,
    onNext: () -> Unit,
    pose: MascotPose = MascotPose.HELP
) {
    var isMounted by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            isMounted = true
        } else {
            delay(250.milliseconds)
            isMounted = false
        }
    }

    if (!isMounted) return

    Popup(
        alignment = Alignment.BottomEnd,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false
        )
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it / 2 },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(250)) + scaleOut(
                targetScale = 0.85f,
                animationSpec = tween(250)
            )
        ) {
            Column(
                modifier = Modifier.padding(end = 12.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Box(modifier = Modifier.widthIn(max = 280.dp)) {
                    MascotSpeechBubble {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.padding(top = 8.dp))
                        Button(onClick = onNext, modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                stringResource(
                                    if (isLastStep) R.string.tour_finish else R.string.tour_next
                                )
                            )
                        }
                    }
                }
                MascotImage(
                    pose = pose,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(220.dp)
                )
            }
        }
    }
}