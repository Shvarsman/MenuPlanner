package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape

/**
 * Единая строка ингредиента: иконка продукта слева, название + количество,
 * опционально индикатор доступности (хватает/не хватает) и опционально
 * кнопка удаления. Используется и в редакторе рецепта (с удалением),
 * и в read-only предпросмотре (WeekMenuScreen и т.п.).
 */
@Composable
fun IngredientListItem(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier,
    availability: IngredientAvailability? = null,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductIcon(product = ingredient.product, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.product.localizedName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (ingredient.product.isToTaste) {
                    stringResource(R.string.to_taste)
                } else {
                    "${formatIngredientQty(ingredient.quantity)} ${stringResource(ingredient.unit.labelRes)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (availability != null && !ingredient.product.isToTaste) {
            Spacer(Modifier.width(8.dp))
            when (availability) {
                IngredientAvailability.AVAILABLE -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.available),
                    contentDescription = stringResource(R.string.ingredient_available),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )

                IngredientAvailability.INSUFFICIENT -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.unavailable),
                    contentDescription = stringResource(R.string.ingredient_available),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.ingredient_available)
                )
            }
        }
    }
}

/**
 * Карточка со списком ингредиентов — единый Surface(CornerShape) с рядами
 * IngredientListItem, разделёнными тонкими HorizontalDivider, вместо набора
 * несвязанных строк без общей границы.
 */
@Composable
fun IngredientListCard(
    ingredients: List<RecipeIngredient>,
    modifier: Modifier = Modifier,
    availabilityFor: (RecipeIngredient) -> IngredientAvailability? = { null },
    onRemove: ((RecipeIngredient) -> Unit)? = null,
    onIngredientClick: ((RecipeIngredient) -> Unit)? = null
) {
    Surface(
        shape = CornerShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            ingredients.forEachIndexed { index, ingredient ->
                IngredientListItem(
                    ingredient = ingredient,
                    availability = availabilityFor(ingredient),
                    onRemove = onRemove?.let { remove -> { remove(ingredient) } },
                    onClick = onIngredientClick?.let { click -> { click(ingredient) } }
                )
                if (index != ingredients.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
            }
        }
    }
}

private fun formatIngredientQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()