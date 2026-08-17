package com.shvarsman.coolinar.presentation.screens.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.R

/** Роль маскота — какая ситуация в приложении его показывает. */
enum class MascotPose(@DrawableRes val drawableRes: Int) {
    NEUTRAL(R.drawable.mascot_neutral),
    SEARCHING(R.drawable.mascot_searching),
    HAPPY(R.drawable.mascot_happy),
    EXCITED(R.drawable.mascot_excited),
    THINKING(R.drawable.mascot_thinking),
    WORRIED(R.drawable.mascot_worried),
    SAD(R.drawable.mascot_sad),
    WAVING(R.drawable.mascot_waving),
    CONFUSED(R.drawable.mascot_confused),
    SLEEPY(R.drawable.mascot_sleepy),
    HELP(R.drawable.mascot_help)
}

/** Имя маскота — используется как contentDescription для доступности. */
const val MASCOT_NAME = "Ирадий"

/** Сам маскот — без текста, для встраивания в произвольную композицию. */
@Composable
fun MascotImage(
    pose: MascotPose,
    modifier: Modifier = Modifier
) {
    MascotImage(drawableRes = pose.drawableRes, modifier = modifier)
}

/**
 * Перегрузка для сюжетных иллюстраций вне общего набора эмоций MascotPose —
 * например, сценки онбординга, которые не переиспользуются больше нигде.
 */
@Composable
fun MascotImage(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = MASCOT_NAME
) {
    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

/**
 * Готовый блок пустого/пограничного состояния: маскот + заголовок +
 * опциональный подзаголовок + опциональное действие (кнопка). Единая точка
 * для всех "тут пока пусто" / "ничего не найдено" / "что-то пошло не так" —
 * чтобы одна и та же композиция не собиралась вручную на каждом экране.
 */
@Composable
fun MascotEmptyState(
    pose: MascotPose,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    size: Dp = 160.dp,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MascotImage(pose = pose, modifier = Modifier.height(size))
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/**
 * Форма "облако мыслей" — прямоугольник со скруглёнными углами и треугольным
 * хвостиком сверху по центру, направленным к маскоту над облаком. Строится
 * через объединение (Union) прямоугольника и треугольника в один Path,
 * без сторонних зависимостей.
 */
@Composable
private fun rememberSpeechBubbleShape(
    cornerRadius: Dp = 24.dp,
    tailWidth: Dp = 28.dp,
    tailHeight: Dp = 14.dp
): Shape {
    val density = LocalDensity.current
    return remember(density, cornerRadius, tailWidth, tailHeight) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val cornerPx = with(density) { cornerRadius.toPx() }
                val tailWidthPx = with(density) { tailWidth.toPx() }
                val tailHeightPx = with(density) { tailHeight.toPx() }
                val tailCenterX = size.width / 2f
                val bodyHeight = size.height - tailHeightPx

                val bodyPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(0f, 0f, size.width, bodyHeight),
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    )
                }
                val tailPath = Path().apply {
                    moveTo(tailCenterX - tailWidthPx / 2f, bodyHeight)
                    lineTo(tailCenterX + tailWidthPx / 2f, bodyHeight)
                    lineTo(tailCenterX, size.height)
                    close()
                }
                val combined = Path().apply { op(bodyPath, tailPath, PathOperation.Union) }
                return Outline.Generic(combined)
            }
        }
    }
}

/**
 * Облако мыслей маскота: та же форма контейнера (Surface + colorScheme.surface),
 * что у остальных карточек в проекте, но со скруглённой формой-выноской вместо
 * CornerShape. Ставится ПОД MascotImage — хвостик указывает вверх, к маскоту.
 */
@Composable
fun MascotSpeechBubble(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tailHeight = 14.dp
    val shape = rememberSpeechBubbleShape(tailHeight = tailHeight)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 16.dp + tailHeight,
                start = 20.dp,
                end = 20.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}