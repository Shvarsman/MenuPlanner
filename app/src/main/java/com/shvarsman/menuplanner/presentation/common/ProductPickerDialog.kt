package com.shvarsman.menuplanner.presentation.common

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.fridge.ProductIcon
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

    val coroutineScope = rememberCoroutineScope()

    val filtered = remember(query, catalog) {
        if (query.isBlank()) catalog else catalog.filter {
            it.name.contains(
                query,
                ignoreCase = true
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (step) {
                    PickerStep.SELECT -> "Выбрать продукт"
                    PickerStep.CREATE -> "Новый продукт"
                    PickerStep.QUANTITY -> selectedProduct?.name.orEmpty()
                }
            )
        },
        text = {
            when (step) {
                PickerStep.SELECT -> Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Поиск продукта") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppCornerRadius)
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { newName = query; step = PickerStep.CREATE },
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
                        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
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

                PickerStep.CREATE -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Категория", style = MaterialTheme.typography.labelLarge)
                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        items(Category.entries.toTypedArray()) { category ->
                            ListItem(
                                headlineContent = { Text(category.displayName) },
                                leadingContent = { Icon(category.icon, contentDescription = null) },
                                trailingContent = {
                                    RadioButton(
                                        selected = category == newCategory,
                                        onClick = { newCategory = category })
                                },
                                modifier = Modifier.clickable { newCategory = category }
                            )
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = unitMenuExpanded,
                        onExpandedChange = { unitMenuExpanded = it }) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedUnit.displayName,
                            onValueChange = {},
                            label = { Text("Ед. изм. по умолчанию") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                        )
                        ExposedDropdownMenu(
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

                PickerStep.QUANTITY -> {
                    val product = selectedProduct
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                value = quantityText,
                                onValueChange = {
                                    quantityText = it.filter { c -> c.isDigit() || c == '.' }
                                },
                                label = { Text("Количество") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = unitMenuExpanded,
                                onExpandedChange = { unitMenuExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedUnit.displayName,
                                    onValueChange = {},
                                    label = { Text("Ед. изм.") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = unitMenuExpanded
                                        )
                                    },
                                    modifier = Modifier.menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    )
                                )
                                ExposedDropdownMenu(
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
                }
            }
        },
        confirmButton = {
            when (step) {
                PickerStep.SELECT -> {}
                PickerStep.CREATE -> TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        coroutineScope.launch {
                            val created = onCreateProduct(newName.trim(), newCategory, selectedUnit)
                            selectedProduct = created
                            quantityText = "1"
                            step = PickerStep.QUANTITY
                        }
                    }
                ) { Text("Далее") }

                PickerStep.QUANTITY -> TextButton(onClick = {
                    val product = selectedProduct ?: return@TextButton
                    onConfirm(product, selectedUnit, quantityText.toDoubleOrNull() ?: 0.0)
                }) { Text("Добавить") }
            }
        },
        dismissButton = {
            when (step) {
                PickerStep.SELECT -> TextButton(onClick = onDismiss) { Text("Отмена") }
                else -> TextButton(onClick = { step = PickerStep.SELECT }) {
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
    )
}

private enum class PickerStep { SELECT, CREATE, QUANTITY }