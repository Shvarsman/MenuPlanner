package com.shvarsman.coolinar.presentation.screens.cookselection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookSelectionScreen(
    onBack: () -> Unit,
    onNavigateToCooking: () -> Unit,
    viewModel: CookSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToCooking) {
        if (uiState.navigateToCooking) {
            onNavigateToCooking()
            viewModel.onNavigateToCookingConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.cook_selection_title)) },
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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.onStartCooking() },
                        enabled = uiState.canStartCooking,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.start_cooking))
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.canStartCooking) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = uiState.selectedCount.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (uiState.canStartCooking) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "next_week_toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.include_next_week),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.includeNextWeek,
                        onCheckedChange = { viewModel.toggleIncludeNextWeek(it) }
                    )
                }
            }

            if (uiState.dayGroups.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.not_planned),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.dayGroups.forEach { group ->
                item(key = "header_${group.date}") {
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                items(group.entries, key = { it.entry.id }) { cookable ->
                    val selectedItem = uiState.selectedByRecipeId[cookable.entry.recipeId]
                    val isSelected = selectedItem?.menuEntryIds?.contains(cookable.entry.id) == true
                    CookSelectionCard(
                        cookable = cookable,
                        isSelected = isSelected,
                        selectedCountForRecipe = selectedItem?.menuEntryIds?.size ?: 0,
                        onClick = { viewModel.onEntryClick(cookable) }
                    )
                }
            }
        }
    }

    val dialogEntries = uiState.duplicateDialogEntries
    if (dialogEntries != null) {
        val recipeId = dialogEntries.first().entry.recipeId
        DuplicateRecipeDialog(
            occurrences = dialogEntries,
            alreadySelectedIds = uiState.selectedByRecipeId[recipeId]?.menuEntryIds?.toSet()
                .orEmpty(),
            onConfirm = { chosen -> viewModel.confirmDuplicateSelection(recipeId, chosen) },
            onDismiss = { viewModel.dismissDuplicateDialog() }
        )
    }
}

@Composable
private fun CookSelectionCard(
    modifier: Modifier = Modifier,
    cookable: CookableEntry,
    isSelected: Boolean,
    selectedCountForRecipe: Int,
    onClick: () -> Unit
) {
    val entry = cookable.entry

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CornerShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.recipePhotoUri != null) {
                AsyncImage(
                    model = rememberSizedImageRequest(entry.recipePhotoUri, 56.dp, 56.dp),
                    contentDescription = entry.recipeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CornerShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CornerShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.recipeTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(entry.mealType.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedCountForRecipe > 1) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "×$selectedCountForRecipe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DuplicateRecipeDialog(
    occurrences: List<CookableEntry>,
    alreadySelectedIds: Set<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val checkedIds = remember(occurrences) {
        mutableStateMapOf<String, Boolean>().apply {
            occurrences.forEach {
                put(
                    it.entry.id,
                    it.entry.id in alreadySelectedIds || alreadySelectedIds.isEmpty()
                )
            }
        }
    }
    val recipeTitle = occurrences.first().entry.recipeTitle

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicate_recipe_title, recipeTitle)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.duplicate_recipe_message, occurrences.size),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                occurrences.forEach { cookable ->
                    val entry = cookable.entry
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { checkedIds[entry.id] = checkedIds[entry.id] != true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedIds[entry.id] == true,
                            onCheckedChange = { checkedIds[entry.id] = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            dateAndMealLabel(
                                cookable,
                                stringResource(cookable.entry.mealType.labelRes)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(occurrences.filter { checkedIds[it.entry.id] == true }
                    .map { it.entry.id })
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun dateAndMealLabel(cookable: CookableEntry, mealLabel: String): String {
    val date = cookable.date
    val dateStr = "${date.dayOfMonth.toString().padStart(2, '0')}.${
        date.monthValue.toString().padStart(2, '0')
    }"
    return "$dateStr — $mealLabel"
}