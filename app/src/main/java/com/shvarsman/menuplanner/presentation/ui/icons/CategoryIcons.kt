package com.shvarsman.menuplanner.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.RecipeCategory

/**
 * Имя SVG-файла выводится из имени enum-константы — специального поля в
 * доменной модели заводить не нужно (в отличие от Product.iconKey, который
 * завязан на свободный пользовательский ввод названия).
 */
private val Category.iconAssetKey: String get() = name.lowercase()
private val RecipeCategory.iconAssetKey: String get() = name.lowercase()
private val CookingMethod.iconAssetKey: String get() = name.lowercase()

/** assets/category_icons/{имя_константы}.svg */
@Composable
fun CategoryIcon(category: Category, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/category_icons/${category.iconAssetKey}.svg",
        contentDescription = null,
        modifier = modifier,
        error = rememberVectorPainter(Icons.Filled.Category) // пока SVG не добавлен — нейтральный фолбэк
    )
}

/** assets/recipe_category_icons/{имя_константы}.svg */
@Composable
fun RecipeCategoryIcon(category: RecipeCategory, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/recipe_category_icons/${category.iconAssetKey}.svg",
        contentDescription = null,
        modifier = modifier,
        error = rememberVectorPainter(Icons.AutoMirrored.Filled.MenuBook)
    )
}

/** assets/cooking_method_icons/{имя_константы}.svg */
@Composable
fun CookingMethodIcon(method: CookingMethod, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/cooking_method_icons/${method.iconAssetKey}.svg",
        contentDescription = null,
        modifier = modifier,
        error = rememberVectorPainter(Icons.Filled.Kitchen)
    )
}