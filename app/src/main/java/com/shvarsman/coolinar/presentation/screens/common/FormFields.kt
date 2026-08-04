@file:OptIn(ExperimentalMaterial3Api::class)

package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.domain.model.MeasureUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Единая "таблетка" количество+единица измерения — редактируемое число слева,
 * кликабельная зона с названием единицы и стрелкой справа. Общий стиль строк
 * ввода (Surface + SearchBarDefaults.colors()) — переиспользуется везде, где
 * нужен такой ввод (ProductPickerDialog, FridgeItemQuantityDialog и далее).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityUnitField(
    quantityText: String,
    onQuantityChange: (String) -> Unit,
    selectedUnit: MeasureUnit,
    onUnitChange: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = quantityText,
                onValueChange = { input ->
                    onQuantityChange(
                        input.filter { c -> c.isDigit() || c == '.' }
                            .let { filtered ->
                                val firstDot = filtered.indexOf('.')
                                if (firstDot == -1) filtered
                                else filtered.substring(0, firstDot + 1) + filtered.substring(
                                    firstDot + 1
                                ).replace(".", "")
                            }
                    )
                },
                placeholder = { Text("Количество") },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Единица измерения",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }) {
                    MeasureUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName) },
                            onClick = { onUnitChange(unit); unitMenuExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

/** Подпись-лейбл над полем ввода в едином стиле — та же роль, что и в ExpirationDatePickerField. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(start = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun ExpirationDatePickerField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 12.dp,
                    top = 14.dp,
                    bottom = 14.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Event, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Не указан",
                    modifier = Modifier.weight(1f)
                )
                if (value != null) {
                    // Без IconButton — его дефолтная зона касания 48dp визуально сдвигала
                    // крестик правее и делала его крупнее остальных иконок в диалоге
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Убрать дату",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { onValueChange(null) }
                    )
                }
            }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(state.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    })
                    showPicker = false
                }) { Text("ОК") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        ) {
            DatePicker(
                state = state,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedYearContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedDayContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    currentYearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedYearContentColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedDayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun ReadOnlyField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}