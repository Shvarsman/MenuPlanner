package com.shvarsman.menuplanner.presentation.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(viewModel: FridgeViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsState()
    val editorOpen by viewModel.editorOpen.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Группируем по категории, порядок — как в enum
    val grouped = remember(products) {
        products
            .groupBy { it.category }
            .toSortedMap(compareBy { it.ordinal })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Холодильник") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onAddClick() }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            EmptyFridgeState(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                grouped.forEach { (category, items) ->
                    item(key = "header_${category.name}") {
                        CategoryHeader(category = category)
                    }
                    items(items, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { viewModel.onEditClick(product) },
                            onDelete = { viewModel.onDeleteClick(product) }
                        )
                    }
                }
            }
        }
    }

    if (editorOpen) {
        ProductEditorDialog(
            initial = editingProduct,
            onDismiss = { viewModel.closeEditor() },
            onConfirm = { name, category, unit, qty ->
                viewModel.saveProduct(name, category, unit, qty)
            }
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("Ок") } },
            title = { Text("Ошибка") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun CategoryHeader(category: Category) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = product.category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatQuantity(product.quantity)} ${product.unit.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
private fun EmptyFridgeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "В холодильнике пока пусто.\nДобавьте первый продукт.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()