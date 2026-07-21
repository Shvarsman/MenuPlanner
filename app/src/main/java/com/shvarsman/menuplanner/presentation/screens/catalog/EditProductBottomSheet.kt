package com.shvarsman.menuplanner.presentation.screens.catalog

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.screens.common.AppBottomSheet
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductBottomSheet(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: Category, unit: MeasureUnit) -> Unit
) {
    var name by remember(product.id) { mutableStateOf(product.name) }
    var category by remember(product.id) { mutableStateOf(product.category) }
    var unit by remember(product.id) { mutableStateOf(product.defaultUnit) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    AppBottomSheet(
        title = "Редактировать продукт",
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { onClose ->
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = unitMenuExpanded,
            onExpandedChange = { unitMenuExpanded = it }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = unit.displayName,
                onValueChange = {},
                label = { Text("Ед. изм. по умолчанию") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
            )
            ExposedDropdownMenu(
                expanded = unitMenuExpanded,
                onDismissRequest = { unitMenuExpanded = false }
            ) {
                MeasureUnit.entries.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(u.displayName) },
                        onClick = { unit = u; unitMenuExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Категория", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)

        LazyColumn(modifier = Modifier
            .heightIn(max = 260.dp)
            .weight(1f)) {
            items(Category.entries.toTypedArray()) { cat ->
                ListItem(
                    headlineContent = { Text(cat.displayName) },
                    leadingContent = {
                        CategoryIcon(
                            modifier = Modifier.size(24.dp),
                            category = category
                        )
                    },
                    trailingContent = {
                        RadioButton(selected = cat == category, onClick = null)
                    },
                    modifier = Modifier
                        .then(
                            androidx.compose.ui.Modifier
                        )
                        .clickable { category = cat }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { Text("Отмена") }
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), category, unit) }
            ) { Text("Сохранить") }
        }
    }
}