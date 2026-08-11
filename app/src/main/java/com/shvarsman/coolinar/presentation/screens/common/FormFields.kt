@file:OptIn(ExperimentalMaterial3Api::class)

package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
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
        shape = CornerShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
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
                placeholder = { Text(stringResource(R.string.quantity_label)) },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = transparentFieldColors(unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.weight(1f)
            )

            Box {
                Row(
                    modifier = Modifier
                        .clip(CornerShape)
                        .clickable { unitMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(selectedUnit.labelRes),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(R.string.unit_of_measure),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }) {
                    MeasureUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(stringResource(unit.labelRes)) },
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
            shape = CornerShape,
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
                    text = value?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        ?: stringResource(R.string.not_specified),
                    modifier = Modifier.weight(1f)
                )
                if (value != null) {
                    // Без IconButton — его дефолтная зона касания 48dp визуально сдвигала
                    // крестик правее и делала его крупнее остальных иконок в диалоге
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.remove_date),
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
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPicker = false
                }) { Text(stringResource(R.string.cancel)) }
            },
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
            shape = CornerShape,
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

/**
 * Обычное текстовое поле с подписью-лейбл сверху — единый стиль для всех
 * форм ввода вне QuantityUnitField (профиль, онбординг, настройки и т.п.).
 */
@Composable
fun LabeledTextField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    textStyle: TextStyle = LocalTextStyle.current
) {
    Column(modifier = modifier) {
        label?.let { FieldLabel(it) }
        Surface(
            shape = CornerShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder?.let { { Text(it) } },
                singleLine = singleLine,
                minLines = minLines,
                isError = isError,
                textStyle = textStyle,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentFieldColors()
            )
        }
    }
}

/** То же самое, что LabeledTextField, но со скрытием текста и переключателем видимости. */
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        FieldLabel(label)
        Surface(
            shape = CornerShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                isError = isError,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentFieldColors()
            )
        }
    }
}

/**
 * Карточка-переход на другой экран: иконка + текст слева, стрелка справа.
 * Единый стиль для всех навигационных пунктов (профиль, бэкап, меню на неделю,
 * список покупок и т.д.) — раньше дублировался в HomeScreen и ProfileScreen.
 */
@Composable
fun NavRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CornerShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint)
                Spacer(Modifier.width(12.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium, color = tint)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = tint)
        }
    }
}

/**
 * Карточка-секция формы: Card(shape=CornerShape) + Column с внутренним отступом 16dp.
 * Убирает повторяющийся Card { Column(Modifier.padding(16.dp)) { ... } } по всему проекту.
 */
@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth(), shape = CornerShape) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/**
 * Общий набор цветов для всех текстовых полей проекта: рамка OutlinedTextField
 * скрыта (Color.Transparent), потому что визуальный контейнер — это Surface
 * снаружи (CornerShape), а не сама рамка поля. unfocusedTextColor параметризован —
 * QuantityUnitField намеренно приглушает текст в неактивном состоянии,
 * остальные поля используют цвет по умолчанию.
 */
@Composable
private fun transparentFieldColors(
    unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
    focusedTextColor: Color = MaterialTheme.colorScheme.onSurface
) = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedTextColor = unfocusedTextColor,
    focusedTextColor = focusedTextColor,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)