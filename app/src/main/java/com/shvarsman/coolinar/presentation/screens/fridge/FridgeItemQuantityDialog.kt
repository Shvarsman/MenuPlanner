package com.shvarsman.coolinar.presentation.screens.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.presentation.screens.common.ExpirationDatePickerField
import com.shvarsman.coolinar.presentation.screens.common.FieldLabel
import com.shvarsman.coolinar.presentation.screens.common.QuantityUnitField
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
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
    var expirationDate by remember { mutableStateOf(item.expirationDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(12.dp))
                ProductIcon(product = item.product, modifier = Modifier.size(48.dp))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FieldLabel("Количество")
                QuantityUnitField(
                    quantityText = quantityText,
                    onQuantityChange = { quantityText = it },
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it }
                )

                Spacer(Modifier.height(8.dp))

                FieldLabel("Срок годности")
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
}