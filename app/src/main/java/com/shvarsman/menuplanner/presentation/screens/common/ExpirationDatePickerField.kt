package com.shvarsman.menuplanner.presentation.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Необязательное поле выбора срока годности — открывает стандартный M3 DatePickerDialog. */
@OptIn(ExperimentalMaterial3Api::class)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(28.dp),
            color = SearchBarDefaults.colors().containerColor,
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
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = state)
        }
    }
}