package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.repository.TipsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MascotTipViewModel @Inject constructor(
    private val tipsRepository: TipsRepository
) : ViewModel() {

    /** Одноразовое чтение, а не непрерывная подписка — сознательно НЕ StateFlow.
     * Раньше здесь был .stateIn(...), создававший новый Flow при каждом вызове
     * из composable (то есть при каждой рекомпозиции экрана) — это порождало
     * новую подписку на DataStore каждый раз и на долю секунды отдавало
     * промежуточное "не видел" (null/false) до того, как реальное значение
     * долетало, из-за чего подсказка мигала. Одноразовое чтение полностью
     * убирает эту гонку. */
    suspend fun isTipSeenOnce(tipId: String): Boolean = tipsRepository.isTipSeen(tipId).first()

    fun markSeen(tipId: String) {
        viewModelScope.launch { tipsRepository.markTipSeen(tipId) }
    }
}

/**
 * Приветственная подсказка маскота при первом визите на экран — не диалог,
 * а оверлей поверх всего контента, выезжающий из угла экрана: облако мыслей
 * с текстом и крестиком-закрытием сверху, маскот рядом. Показывается один
 * раз за всё время использования приложения для данного tipId.
 */
@Composable
fun MascotWelcomeTip(
    tipId: String,
    message: String,
    pose: MascotPose = MascotPose.HELP,
    enabled: Boolean = true,
    viewModel: MascotTipViewModel = hiltViewModel()
) {
    var isVisible by remember(tipId) { mutableStateOf(false) }
    var isMounted by remember(tipId) { mutableStateOf(false) }
    // Уже проверяли DataStore и подсказку либо решили не показывать (уже видел),
    // либо ждём, пока enabled станет true (контент экрана загрузится) — второй
    // раз запрос к DataStore не шлём при каждой рекомпозиции.
    var checked by remember(tipId) { mutableStateOf(false) }

    LaunchedEffect(tipId, enabled) {
        if (checked || !enabled) return@LaunchedEffect
        val alreadySeen = viewModel.isTipSeenOnce(tipId)
        checked = true
        if (!alreadySeen) {
            isMounted = true
            isVisible = true
        }
    }

    fun dismiss() {
        // Помечаем увиденным ТОЛЬКО здесь, по явному закрытию — а не в момент
        // показа. Если помечать сразу при показе, любой перерендер экрана,
        // из-за которого LaunchedEffect(tipId) выше перезапускается, находит
        // tipId уже помеченным и подсказка больше не появляется — снаружи это
        // выглядит как "показалась и тут же пропала".
        viewModel.markSeen(tipId)
        isVisible = false
    }

    if (isMounted) {
        LaunchedEffect(isVisible) {
            if (!isVisible) {
                delay(250.milliseconds)
                isMounted = false
            }
        }
        Popup(
            alignment = Alignment.BottomEnd,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
            ) {
                MascotCornerBubble(
                    pose = pose,
                    message = message,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}

@Composable
private fun MascotCornerBubble(
    pose: MascotPose,
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(end = 12.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(modifier = Modifier.widthIn(max = 260.dp)) {
            MascotSpeechBubble {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        MascotImage(
            pose = pose,
            modifier = Modifier.size(220.dp)
        )
    }
}