package com.shvarsman.coolinar.presentation.screens.common

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.shvarsman.coolinar.domain.model.Product

/** Название продукта на текущем языке интерфейса. Для сидовых продуктов
 * (isDefault = true) берётся nameEn на английской локали; для продуктов,
 * созданных пользователем, nameEn всегда равен name, так что показывается
 * то, что пользователь ввёл сам. */
fun Product.localizedName(): String {
    val language = Resources.getSystem().configuration.locales[0].language
    return if (language == "en") nameEn else name
}