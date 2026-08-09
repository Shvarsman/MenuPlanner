package com.shvarsman.coolinar.presentation.screens.backup

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.repository.BackupType
import com.shvarsman.coolinar.presentation.screens.common.FormCard
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
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
                        text = stringResource(R.string.backup_title),
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
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
            FormCard {
                Text(
                    text = stringResource(R.string.backup_full_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.backup_full_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        startExport(
                            BackupType.FULL,
                            fileNamePrefix = "coolinar_full_backup"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is BackupUiState.InProgress
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.backup_full_button))
                }
            }

            FormCard {
                Text(
                    text = stringResource(R.string.backup_recipes_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.backup_recipes_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        startExport(
                            type = BackupType.RECIPES_ONLY,
                            fileNamePrefix = "coolinar_recipes"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is BackupUiState.InProgress
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.backup_recipes_button))
                }
            }

            FormCard {
                Text(
                    text = stringResource(R.string.backup_restore_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.backup_restore_description),
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
                    Text(text = stringResource(R.string.backup_restore_button))
                }
            }
        }
    }

    when (val state = uiState) {
        is BackupUiState.InProgress -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text(text = stringResource(R.string.backup_please_wait)) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(text = stringResource(R.string.backup_processing))
                    }
                }
            )
        }

        is BackupUiState.ExportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearState() }) {
                        Text(
                            text = stringResource(
                                R.string.ok
                            )
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.done)) },
                text = { Text(text = exportSummary(state)) }
            )
        }

        is BackupUiState.ImportSuccess -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearState() }) {
                        Text(
                            text = stringResource(
                                R.string.ok
                            )
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.done)) },
                text = { Text(text = importSummary(state.result)) }
            )
        }

        is BackupUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearState() }) {
                        Text(
                            text = stringResource(
                                R.string.ok
                            )
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.error)) },
                text = { Text(text = state.message) }
            )
        }

        BackupUiState.Idle -> {}
    }
}

@Composable
private fun exportSummary(state: BackupUiState.ExportSuccess): String {
    val r = state.result
    return when (state.type) {
        BackupType.FULL -> stringResource(
            R.string.backup_export_summary_full,
            r.recipesCount,
            r.fridgeItemsCount,
            r.shoppingItemsCount,
            r.menuEntriesCount
        )

        BackupType.RECIPES_ONLY -> stringResource(
            R.string.backup_export_summary_recipes,
            r.recipesCount
        )

        BackupType.SINGLE_RECIPE -> stringResource(R.string.backup_export_summary_single_recipe)
    }
}

@Composable
private fun importSummary(r: com.shvarsman.coolinar.domain.repository.BackupResult): String {
    val parts = mutableListOf<String>()
    if (r.recipesCount > 0) parts.add(
        stringResource(
            R.string.backup_import_part_recipes,
            r.recipesCount
        )
    )
    if (r.fridgeItemsCount > 0) parts.add(
        stringResource(
            R.string.backup_import_part_fridge,
            r.fridgeItemsCount
        )
    )
    if (r.shoppingItemsCount > 0) parts.add(
        stringResource(
            R.string.backup_import_part_shopping,
            r.shoppingItemsCount
        )
    )
    if (r.menuEntriesCount > 0) parts.add(
        stringResource(
            R.string.backup_import_part_menu,
            r.menuEntriesCount
        )
    )
    return if (parts.isEmpty()) {
        stringResource(R.string.backup_import_empty)
    } else {
        stringResource(R.string.backup_import_summary, parts.joinToString(", "))
    }
}