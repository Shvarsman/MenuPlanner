package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.presentation.screens.common.ExpirationDatePickerField
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeItemQuantityDialog(
    item: FridgeItem,
    onDismiss: () -> Unit,
    onConfirm: (unit: MeasureUnit, quantity: Double, expirationDate: LocalDate?) -> Unit
) {
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    var selectedUnit by remember { mutableStateOf(item.unit) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var expirationDate by remember { mutableStateOf(item.expirationDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.product.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
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
                                    onClick = { selectedUnit = unit; unitMenuExpanded = false }
                                )
                            }
                        }
                    }
                }

                ExpirationDatePickerField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedUnit, quantityText.toDoubleOrNull() ?: 0.0, expirationDate)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}