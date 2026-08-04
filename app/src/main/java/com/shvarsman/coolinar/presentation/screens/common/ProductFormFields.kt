@file:OptIn(ExperimentalMaterial3Api::class)

package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.presentation.ui.icons.CategoryIcon

/** Поле названия продукта — в едином стиле с остальными полями ввода. */
@Composable
fun ProductNameField(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        FieldLabel("Название")
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("Например, Арбуз") },
                    singleLine = true,
                    isError = isError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Список выбора категории — прокручиваемый, с радио-кнопкой на каждой строке. */
@Composable
fun CategoryPickerList(
    selectedCategory: Category,
    onCategoryChange: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FieldLabel("Категория")
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
        ) {
            items(Category.entries.toTypedArray()) { category ->
                ListItem(
                    headlineContent = { Text(category.displayName) },
                    leadingContent = {
                        CategoryIcon(category = category, modifier = Modifier.size(24.dp))
                    },
                    trailingContent = {
                        RadioButton(selected = category == selectedCategory, onClick = null)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        headlineColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.clickable { onCategoryChange(category) }
                )
            }
        }
    }
}

/** Выбор единицы измерения — кликабельная "таблетка" с выпадающим меню. */
@Composable
fun MeasureUnitField(
    selectedUnit: MeasureUnit,
    onUnitChange: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Единица измерения по умолчанию"
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        FieldLabel(label)
        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 14.dp,
                        bottom = 14.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedUnit.displayName, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MeasureUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName) },
                        onClick = { onUnitChange(unit); expanded = false }
                    )
                }
            }
        }
    }
}

/**
 * Все три поля вместе (название + категория + единица измерения) — стандартная
 * "форма продукта", используется и при создании нового продукта, и при
 * редактировании существующего.
 */
@Composable
fun ProductFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    category: Category,
    onCategoryChange: (Category) -> Unit,
    unit: MeasureUnit,
    onUnitChange: (MeasureUnit) -> Unit,
    isToTaste: Boolean,
    onIsToTasteChange: (Boolean) -> Unit,
    isAlwaysAvailable: Boolean,
    onIsAlwaysAvailableChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    nameError: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProductNameField(name = name, onNameChange = onNameChange, isError = nameError)
        CategoryPickerList(
            selectedCategory = category,
            onCategoryChange = onCategoryChange,
            modifier = Modifier.weight(1f)
        )
        MeasureUnitField(selectedUnit = unit, onUnitChange = onUnitChange)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "По вкусу",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Без фиксированного количества — соль, специи и т.п.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = isToTaste, onCheckedChange = onIsToTasteChange)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Всегда в наличии",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Вода из-под крана и т.п. — не хранится в холодильнике, не докупается",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = isAlwaysAvailable, onCheckedChange = onIsAlwaysAvailableChange)
        }
    }
}