package com.shvarsman.menuplanner.presentation.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
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
                "В резервную копию попадают продукты и рецепты (включая фото, ингредиенты и шаги приготовления). Холодильник, меню на неделю и список покупок не сохраняются.",
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
                text = { Text("Сохранено продуктов: ${state.productsCount}, рецептов: ${state.recipesCount}") }
            )
        }

        is BackupUiState.ImportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text("Ок") } },
                title = { Text("Готово") },
                text = { Text("Восстановлено продуктов: ${state.productsCount}, рецептов: ${state.recipesCount}") }
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