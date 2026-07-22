package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.screens.fridge.ProductIcon
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import kotlinx.coroutines.launch

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
    onConfirm: (product: Product, unit: MeasureUnit, quantity: Double) -> Unit,
    onCreateProduct: suspend (name: String, category: Category, unit: MeasureUnit) -> Product
) {
    var step by remember { mutableStateOf(PickerStep.SELECT) }
    var query by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

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
            PickerStep.QUANTITY -> selectedProduct?.name.orEmpty()
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
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Поиск продукта") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
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
                    HorizontalDivider()

                    if (filtered.isEmpty()) {
                        Text(
                            "Ничего не найдено",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
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
                                    modifier = Modifier.clickable {
                                        if (product.isToTaste) {
                                            // Специи/соль/сахар и т.п. — без указания количества
                                            onConfirm(product, product.defaultUnit, 0.0)
                                        } else {
                                            selectedProduct = product
                                            selectedUnit = product.defaultUnit
                                            quantityText = "1"
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = newName,
                            onValueChange = {
                                newName = it
                                createError = null
                            },
                            label = { Text("Название") },
                            singleLine = true,
                            isError = createError != null,
                            supportingText = createError?.let { { Text(it) } },
                            shape = RoundedCornerShape(24.dp)
                        )

                        Text("Категория", style = MaterialTheme.typography.labelLarge)
                        LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
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
                                            // клик обрабатывает родительский Modifier.clickable —
                                            // не дублируем интерактивный элемент внутри строки
                                            onClick = null
                                        )
                                    },
                                    modifier = Modifier.clickable { newCategory = category }
                                )
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = unitMenuExpanded,
                            onExpandedChange = { unitMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ),
                                readOnly = true,
                                value = selectedUnit.displayName,
                                onValueChange = {},
                                label = { Text("Ед. изм. по умолчанию") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded)
                                },
                                shape = RoundedCornerShape(24.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false }
                            ) {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (product != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProductIcon(
                                    product = product,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Row {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = quantityText,
                                onValueChange = {
                                    quantityText = it.filter { c -> c.isDigit() || c == '.' }
                                        .let { filtered ->
                                            // не даём ввести больше одной десятичной точки
                                            val firstDot = filtered.indexOf('.')
                                            if (firstDot == -1) filtered
                                            else filtered.substring(0, firstDot + 1) +
                                                    filtered.substring(firstDot + 1)
                                                        .replace(".", "")
                                        }
                                },
                                label = { Text("Количество") },
                                singleLine = true,
                                isError = quantityText.isNotEmpty() && parsedQuantity == null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = unitMenuExpanded,
                                onExpandedChange = { unitMenuExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ),
                                    readOnly = true,
                                    value = selectedUnit.displayName,
                                    onValueChange = {},
                                    label = { Text("Ед. изм.") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = unitMenuExpanded
                                        )
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                )
                                ExposedDropdownMenu(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.05f
                                                    ),
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.02f
                                                    )
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp),
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.6f
                                                    ),
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    expanded = unitMenuExpanded,
                                    onDismissRequest = { unitMenuExpanded = false },
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 0.dp
                                ) {
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
                        onConfirm(product, selectedUnit, parsedQuantity ?: 0.0)
                    }
                ) { Text("Добавить") }
            }
        }
    }
}

private enum class PickerStep { SELECT, CREATE, QUANTITY }