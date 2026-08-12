package com.shvarsman.coolinar.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shvarsman.coolinar.R

/**
 * Форматирует время готовки в "1 ч 30 мин" / "45 мин" — используется на
 * карточках рецептов вместо ingredientCount/stepCount. null (время не
 * указано) обрабатывается на уровне вызывающего кода, а не здесь.
 */
@Composable
fun formatCookingTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours, mins)
    } else {
        stringResource(R.string.duration_minutes, mins)
    }
}