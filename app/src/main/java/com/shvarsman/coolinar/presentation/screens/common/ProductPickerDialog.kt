package com.shvarsman.coolinar.presentation.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Универсальный диалог: поиск/выбор продукта из каталога, создание нового продукта,
 * указание количества и единицы измерения. Используется в Холодильнике, Рецептах
 * и Списке покупок.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerDialog(
    modifier: Modifier = Modifier,
    catalog: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (product: Product, unit: MeasureUnit, quantity: Double, expirationDate: LocalDate?) -> Unit,
    onCreateProduct: suspend (
        name: String, category: Category, unit: MeasureUnit,
        isToTaste: Boolean, isAlwaysAvailable: Boolean
    ) -> Product,
    showExpirationDate: Boolean = true
) {
    var step by remember { mutableStateOf(PickerStep.SELECT) }
    var query by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var expirationDate by remember { mutableStateOf<LocalDate?>(null) }

    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(Category.GROCERY) }
    var newIsToTaste by remember { mutableStateOf(false) }
    var newIsAlwaysAvailable by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val createProductErrorTemplate = stringResource(R.string.create_product_error)

    val filtered = remember(query, catalog) {
        if (query.isBlank()) catalog else catalog.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    val parsedQuantity = quantityText.toDoubleOrNull()

    AppBottomSheet(
        modifier = modifier,
        title = when (step) {
            PickerStep.SELECT -> stringResource(R.string.select_product)
            PickerStep.CREATE -> stringResource(R.string.new_product)
            PickerStep.QUANTITY -> ""
        },
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { onClose ->

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (step) {
                PickerStep.SELECT -> {
                    TopBarSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = stringResource(R.string.search_product),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            newName = query
                            createError = null
                            step = PickerStep.CREATE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.create_new_product))
                    }

                    if (filtered.isEmpty()) {
                        MascotEmptyState(
                            pose = MascotPose.SEARCHING,
                            title = stringResource(R.string.nothing_found),
                            subtitle = stringResource(R.string.nothing_found_hint),
                            size = 120.dp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CornerShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            items(filtered, key = { it.id }) { product ->
                                ListItem(
                                    headlineContent = { Text(product.name) },
                                    supportingContent = { Text(product.category.displayName) },
                                    leadingContent = {
                                        ProductIcon(
                                            product = product,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent,
                                        headlineColor = MaterialTheme.colorScheme.onSurface,
                                        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        if (product.isToTaste) {
                                            onConfirm(product, product.defaultUnit, 0.0, null)
                                        } else {
                                            selectedProduct = product
                                            selectedUnit = product.defaultUnit
                                            quantityText = "1"
                                            step = PickerStep.QUANTITY
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                PickerStep.CREATE -> {
                    ProductFormFields(
                        name = newName,
                        onNameChange = { newName = it; createError = null },
                        category = newCategory,
                        onCategoryChange = { newCategory = it },
                        unit = selectedUnit,
                        onUnitChange = { selectedUnit = it },
                        isToTaste = newIsToTaste,
                        onIsToTasteChange = { newIsToTaste = it },
                        isAlwaysAvailable = newIsAlwaysAvailable,
                        onIsAlwaysAvailableChange = { newIsAlwaysAvailable = it },
                        nameError = createError != null,
                        modifier = Modifier.weight(1f)
                    )
                    createError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                PickerStep.QUANTITY -> {
                    val product = selectedProduct
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (product != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = product.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(16.dp))
                                ProductIcon(
                                    product = product,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        FieldLabel(stringResource(R.string.quantity_label))
                        QuantityUnitField(
                            quantityText = quantityText,
                            onQuantityChange = { quantityText = it },
                            selectedUnit = selectedUnit,
                            onUnitChange = { selectedUnit = it },
                            isError = quantityText.isNotEmpty() && parsedQuantity == null
                        )
                        if (showExpirationDate) {
                            Spacer(Modifier.height(8.dp))
                            ExpirationDatePickerField(
                                value = expirationDate,
                                onValueChange = { expirationDate = it },
                                label = stringResource(R.string.expiration_date_label)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (step) {
                PickerStep.SELECT -> {
                    TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) }
                    Spacer(Modifier.width(0.dp))
                }

                else -> {
                    TextButton(onClick = { step = PickerStep.SELECT }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.back))
                    }
                }
            }

            when (step) {
                PickerStep.SELECT -> {}

                PickerStep.CREATE -> Button(
                    enabled = newName.isNotBlank() && !isCreating,
                    onClick = {
                        isCreating = true
                        coroutineScope.launch {
                            try {
                                val created = onCreateProduct(newName.trim(), newCategory, selectedUnit, newIsToTaste, newIsAlwaysAvailable)
                                selectedProduct = created
                                quantityText = "1"
                                expirationDate = null
                                createError = null
                                step = PickerStep.QUANTITY
                            } catch (e: Exception) {
                                createError = String.format(createProductErrorTemplate, e.localizedMessage)
                            } finally {
                                isCreating = false
                            }
                        }
                    }
                ) { Text(if (isCreating) stringResource(R.string.creating) else stringResource(R.string.next)) }

                PickerStep.QUANTITY -> Button(
                    enabled = parsedQuantity != null && parsedQuantity > 0,
                    onClick = {
                        val product = selectedProduct ?: return@Button
                        onConfirm(product, selectedUnit, parsedQuantity ?: 0.0, expirationDate)
                    }
                ) { Text(stringResource(R.string.add)) }
            }
        }
    }
}

private enum class PickerStep { SELECT, CREATE, QUANTITY }