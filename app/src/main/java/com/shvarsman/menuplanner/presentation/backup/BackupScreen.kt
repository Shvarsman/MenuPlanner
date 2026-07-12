package com.shvarsman.menuplanner.presentation.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let { viewModel.onExport(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.onImport(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Резервное копирование",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "В резервную копию попадают рецепты (включая фото, ингредиенты и шаги) и количество продуктов в холодильнике. Каталог продуктов, меню на неделю и список покупок не сохраняются — каталог продуктов встроен в приложение и одинаков у всех пользователей.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElevatedCard(shape = RoundedCornerShape(AppCornerRadius)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Экспорт", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Сохранить продукты и рецепты в файл .zip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat(
                                "yyyy-MM-dd_HHmm",
                                Locale.getDefault()
                            ).format(Date())
                            exportLauncher.launch("menuplanner_backup_$timestamp.zip")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.InProgress
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Создать резервную копию")
                    }
                }
            }

            ElevatedCard(shape = RoundedCornerShape(AppCornerRadius)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Восстановление", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Продукты объединяются с существующими по названию. Рецепты всегда добавляются как новые — повторное восстановление одного и того же файла создаст дубликаты рецептов.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            importLauncher.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/octet-stream"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.InProgress
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Восстановить из файла")
                    }
                }
            }
        }
    }

    when (val state = uiState) {
        is BackupUiState.InProgress -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Подождите") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Обработка резервной копии...")
                    }
                }
            )
        }

        is BackupUiState.ExportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text("Ок") } },
                title = { Text("Готово") },
                text = { Text("Сохранено продуктов: ${state.fridgeItemsCount}, рецептов: ${state.recipesCount}") }
            )
        }

        is BackupUiState.ImportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text("Ок") } },
                title = { Text("Готово") },
                text = { Text("Восстановлено продуктов: ${state.fridgeItemsCount}, рецептов: ${state.recipesCount}") }
            )
        }

        is BackupUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text("Ок") } },
                title = { Text("Ошибка") },
                text = { Text(state.message) }
            )
        }

        BackupUiState.Idle -> {}
    }
}