package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.screens.fridge.ProductIcon
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Универсальный диалог: поиск/выбор продукта из каталога, создание нового продукта,
 * указание количества и единицы измерения. Используется в Холодильнике, Рецептах
 * и Списке покупок.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerDialog(
    modifier: Modifier = Modifier,
    catalog: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (product: Product, unit: MeasureUnit, quantity: Double, expirationDate: LocalDate?) -> Unit,
    onCreateProduct: suspend (name: String, category: Category, unit: MeasureUnit) -> Product
) {
    var step by remember { mutableStateOf(PickerStep.SELECT) }
    var query by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var expirationDate by remember { mutableStateOf<LocalDate?>(null) }

    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(Category.GROCERY) }
    var createError by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val filtered = remember(query, catalog) {
        if (query.isBlank()) catalog else catalog.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    val parsedQuantity = quantityText.toDoubleOrNull()

    AppBottomSheet(
        modifier = modifier,
        title = when (step) {
            PickerStep.SELECT -> "Выбрать продукт"
            PickerStep.CREATE -> "Новый продукт"
            PickerStep.QUANTITY -> ""
        },
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { onClose ->

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (step) {
                PickerStep.SELECT -> {
                    TopBarSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Поиск продукта",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            newName = query
                            createError = null
                            step = PickerStep.CREATE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Создать новый продукт")
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            "Ничего не найдено",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp))
                        ) {
                            items(filtered, key = { it.id }) { product ->
                                ListItem(
                                    headlineContent = { Text(product.name) },
                                    supportingContent = { Text(product.category.displayName) },
                                    leadingContent = {
                                        ProductIcon(
                                            product = product,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    modifier = Modifier.clickable {
                                        if (product.isToTaste) {
                                            onConfirm(
                                                product,
                                                product.defaultUnit,
                                                0.0,
                                                null
                                            )
                                        } else {
                                            selectedProduct = product
                                            selectedUnit = product.defaultUnit
                                            quantityText = "1"
                                            expirationDate = null
                                            step = PickerStep.QUANTITY
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                PickerStep.CREATE -> {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Название",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = SearchBarDefaults.colors().containerColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = newName,
                                        onValueChange = { newName = it; createError = null },
                                        placeholder = { Text("Например, Арбуз") },
                                        singleLine = true,
                                        isError = createError != null,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Категория",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        LazyColumn(modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(28.dp))) {
                            items(Category.entries.toTypedArray()) { category ->
                                ListItem(
                                    headlineContent = { Text(category.displayName) },
                                    leadingContent = {
                                        CategoryIcon(
                                            modifier = Modifier.size(24.dp),
                                            category = category,
                                        )
                                    },
                                    trailingContent = {
                                        RadioButton(
                                            selected = category == newCategory,
                                            onClick = null
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    modifier = Modifier.clickable { newCategory = category }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Единица измерения по умолчанию",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Box {
                            Surface(
                                onClick = { unitMenuExpanded = true },
                                shape = RoundedCornerShape(28.dp),
                                color = SearchBarDefaults.colors().containerColor,
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
                            DropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false }) {
                                MeasureUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.displayName) },
                                        onClick = { selectedUnit = unit; unitMenuExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                PickerStep.QUANTITY -> {
                    val product = selectedProduct
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (product != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(16.dp))
                                ProductIcon(
                                    product = product,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        Text(
                            text = "Количество",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = SearchBarDefaults.colors().containerColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = quantityText,
                                    onValueChange = {
                                        quantityText = it.filter { c -> c.isDigit() || c == '.' }
                                            .let { filtered ->
                                                val firstDot = filtered.indexOf('.')
                                                if (firstDot == -1) filtered
                                                else filtered.substring(
                                                    0,
                                                    firstDot + 1
                                                ) + filtered.substring(firstDot + 1)
                                                    .replace(".", "")
                                            }
                                    },
                                    placeholder = { Text("Количество") },
                                    singleLine = true,
                                    isError = quantityText.isNotEmpty() && parsedQuantity == null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { unitMenuExpanded = true }
                                            .padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            selectedUnit.displayName,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            contentDescription = "Единица измерения",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = unitMenuExpanded,
                                        onDismissRequest = { unitMenuExpanded = false }) {
                                        MeasureUnit.entries.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit.displayName) },
                                                onClick = {
                                                    selectedUnit = unit; unitMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        ExpirationDatePickerField(
                            value = expirationDate,
                            onValueChange = { expirationDate = it },
                            label = "Срок годности"
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (step) {
                PickerStep.SELECT -> {
                    TextButton(onClick = onClose) { Text("Отмена") }
                    Spacer(Modifier.width(0.dp))
                }

                else -> {
                    TextButton(onClick = { step = PickerStep.SELECT }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Назад")
                    }
                }
            }

            when (step) {
                PickerStep.SELECT -> {}

                PickerStep.CREATE -> Button(
                    enabled = newName.isNotBlank() && !isCreating,
                    onClick = {
                        isCreating = true
                        coroutineScope.launch {
                            try {
                                val created =
                                    onCreateProduct(newName.trim(), newCategory, selectedUnit)
                                selectedProduct = created
                                quantityText = "1"
                                expirationDate = null
                                createError = null
                                step = PickerStep.QUANTITY
                            } catch (e: Exception) {
                                createError = "Не удалось создать продукт: ${e.localizedMessage}"
                            } finally {
                                isCreating = false
                            }
                        }
                    }
                ) { Text(if (isCreating) "Создание..." else "Далее") }

                PickerStep.QUANTITY -> Button(
                    enabled = parsedQuantity != null && parsedQuantity > 0,
                    onClick = {
                        val product = selectedProduct ?: return@Button
                        onConfirm(product, selectedUnit, parsedQuantity ?: 0.0, expirationDate)
                    }
                ) { Text("Добавить") }
            }
        }
    }
}

private enum class PickerStep { SELECT, CREATE, QUANTITY }