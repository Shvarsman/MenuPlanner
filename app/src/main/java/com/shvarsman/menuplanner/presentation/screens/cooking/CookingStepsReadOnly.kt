package com.shvarsman.menuplanner.presentation.screens.cooking

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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

                is StepContentItem.Timer -> {
                    CookingStepTimer(
                        totalMinutes = item.minutes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Интерактивный таймер шага: старт/пауза, обратный отсчёт, сброс и вибрация по завершении.
 * Состояние привязано к ключу LazyColumn-item ("cook_step_$index" в вызывающем коде),
 * поэтому переживает скролл списка внутри этого экрана и поворот экрана (rememberSaveable).
 * Отсчёт идёт, пока композиция жива — свернув приложение или уйдя с экрана, таймер
 * приостановится (для настоящего фонового таймера потребуется отдельный сервис/уведомление).
 */
@Composable
private fun CookingStepTimer(
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val totalSeconds = remember(totalMinutes) { totalMinutes * 60 }
    var remainingSeconds by rememberSaveable(totalMinutes) { mutableStateOf(totalSeconds) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    val isFinished = remainingSeconds <= 0

    val context = LocalContext.current

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (remainingSeconds > 0) {
            delay(1000.milliseconds)
            remainingSeconds -= 1
        }
        isRunning = false
        vibrateOnFinish(context)
    }

    val containerColor = when {
        isFinished -> MaterialTheme.colorScheme.errorContainer
        isRunning -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        isFinished -> MaterialTheme.colorScheme.onErrorContainer
        isRunning -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isFinished) "Время вышло" else "Таймер",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatRemaining(remainingSeconds),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = contentColor
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = {
                    if (totalSeconds == 0) 0f else remainingSeconds / totalSeconds.toFloat()
                },
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        if (isFinished) {
                            remainingSeconds = totalSeconds
                            isRunning = true
                        } else {
                            isRunning = !isRunning
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            isFinished -> "Запустить снова"
                            isRunning -> "Пауза"
                            remainingSeconds == totalSeconds -> "Запустить"
                            else -> "Продолжить"
                        }
                    )
                }

                if (remainingSeconds != totalSeconds) {
                    TextButton(
                        onClick = {
                            isRunning = false
                            remainingSeconds = totalSeconds
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Сбросить")
                    }
                }
            }
        }
    }
}

private fun formatRemaining(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun vibrateOnFinish(context: Context) {
    val pattern = longArrayOf(0, 300, 150, 300, 150, 300)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }
}