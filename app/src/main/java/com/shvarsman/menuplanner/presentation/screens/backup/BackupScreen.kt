package com.shvarsman.menuplanner.presentation.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.repository.BackupType
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingExportType by remember { mutableStateOf<BackupType?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val type = pendingExportType
        if (uri != null && type != null) {
            viewModel.onExport(uri, type)
        }
        pendingExportType = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.onImport(it) } }

    fun startExport(type: BackupType, fileNamePrefix: String) {
        pendingExportType = type
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
        exportLauncher.launch("${fileNamePrefix}_$timestamp.zip")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Резервное копирование",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
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
            Card(shape = RoundedCornerShape(AppCornerRadius)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Полное копирование",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Рецепты, холодильник, список покупок и меню на неделю целиком.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            startExport(
                                BackupType.FULL,
                                fileNamePrefix = "menuplanner_full_backup"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.InProgress
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Создать полную копию")
                    }
                }
            }

            Card(shape = RoundedCornerShape(AppCornerRadius)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Только рецепты",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Все рецепты с фото, ингредиентами и шагами — без холодильника, покупок и меню.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { startExport(
                            type = BackupType.RECIPES_ONLY,
                            fileNamePrefix = "menuplanner_recipes"
                        ) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.InProgress
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Сохранить все рецепты")
                    }
                }
            }

            Card(shape = RoundedCornerShape(AppCornerRadius)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Восстановление",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Подходит любой файл резервной копии — полный, только рецепты или один рецепт. Продукты объединяются по названию. Рецепты и записи меню всегда добавляются как новые.",
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
                        Icon(
                            imageVector = Icons.Filled.FileUpload,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Восстановить из файла")
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
                title = { Text(text = "Подождите") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(text = "Обработка резервной копии...")
                    }
                }
            )
        }

        is BackupUiState.ExportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text(text = "Ок") } },
                title = { Text(text = "Готово") },
                text = { Text(text = exportSummary(state)) }
            )
        }

        is BackupUiState.ImportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text(text = "Ок") } },
                title = { Text(text = "Готово") },
                text = { Text(text = importSummary(state.result)) }
            )
        }

        is BackupUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = { TextButton(onClick = { viewModel.clearState() }) { Text(text = "Ок") } },
                title = { Text(text = "Ошибка") },
                text = { Text(text = state.message) }
            )
        }

        BackupUiState.Idle -> {}
    }
}

private fun exportSummary(state: BackupUiState.ExportSuccess): String {
    val r = state.result
    return when (state.type) {
        BackupType.FULL -> {
            "Сохранено: рецептов — ${r.recipesCount}, продуктов в холодильнике — ${r.fridgeItemsCount}, позиций в списке покупок — ${r.shoppingItemsCount}, записей в меню — ${r.menuEntriesCount}"
        }
        BackupType.RECIPES_ONLY -> {
            "Сохранено рецептов: ${r.recipesCount}"
        }
        BackupType.SINGLE_RECIPE -> {
            "Рецепт сохранён"
        }
    }
}

private fun importSummary(r: com.shvarsman.menuplanner.domain.repository.BackupResult): String {
    val parts = mutableListOf<String>()
    if (r.recipesCount > 0) parts.add("рецептов — ${r.recipesCount}")
    if (r.fridgeItemsCount > 0) parts.add("продуктов в холодильнике — ${r.fridgeItemsCount}")
    if (r.shoppingItemsCount > 0) parts.add("позиций в списке покупок — ${r.shoppingItemsCount}")
    if (r.menuEntriesCount > 0) parts.add("записей в меню — ${r.menuEntriesCount}")
    return if (parts.isEmpty()) "Файл не содержал данных для восстановления" else "Восстановлено: ${
        parts.joinToString(
            ", "
        )
    }"
}