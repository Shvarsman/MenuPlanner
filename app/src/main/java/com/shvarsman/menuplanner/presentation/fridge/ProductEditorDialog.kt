package com.shvarsman.menuplanner.presentation.fridge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import androidx.compose.material3.ExposedDropdownMenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: Category, unit: MeasureUnit, quantity: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var quantityText by remember { mutableStateOf(initial?.quantity?.toString() ?: "1") }
    var selectedUnit by remember { mutableStateOf(initial?.unit ?: MeasureUnit.PIECE) }
    var selectedCategory by remember { mutableStateOf(initial?.category ?: Category.GROCERY) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый продукт" else "Изменить продукт") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Категория", style = MaterialTheme.typography.labelLarge)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(Category.entries.toTypedArray()) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(category.displayName, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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
        },
        confirmButton = {
            TextButton(onClick = {
                val quantity = quantityText.toDoubleOrNull() ?: 0.0
                onConfirm(name.trim(), selectedCategory, selectedUnit, quantity)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}