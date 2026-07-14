package com.shvarsman.menuplanner.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.shvarsman.menuplanner.data.backup.BackupFridgeItemDto
import com.shvarsman.menuplanner.data.backup.BackupIngredientDto
import com.shvarsman.menuplanner.data.backup.BackupMenuEntryDto
import com.shvarsman.menuplanner.data.backup.BackupPayload
import com.shvarsman.menuplanner.data.backup.BackupRecipeDto
import com.shvarsman.menuplanner.data.backup.BackupScope
import com.shvarsman.menuplanner.data.backup.BackupShoppingItemDto
import com.shvarsman.menuplanner.data.backup.BackupStepDto
import com.shvarsman.menuplanner.data.local.ImageFileManager
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.BackupResult
import com.shvarsman.menuplanner.domain.repository.BackupType
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.time.DayOfWeek
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private val backupJson = Json { prettyPrint = false; ignoreUnknownKeys = true }

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val fridgeRepository: FridgeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val menuRepository: MenuRepository,
    private val imageFileManager: ImageFileManager
) : BackupRepository {

    override suspend fun exportBackup(destinationUri: Uri, type: BackupType, singleRecipeId: Long?): BackupResult =
        withContext(Dispatchers.IO) {
            exportBackupInternal(destinationUri, type, singleRecipeId)
        }

    override suspend fun importBackup(sourceUri: Uri): BackupResult =
        withContext(Dispatchers.IO) {
            importBackupInternal(sourceUri)
        }

    private suspend fun exportBackupInternal(destinationUri: Uri, type: BackupType, singleRecipeId: Long?): BackupResult {
        val imageFilesToPack = mutableMapOf<String, String>()
        fun registerImage(uriString: String?): String? {
            if (uriString == null) return null
            return imageFilesToPack.getOrPut(uriString) { "img_${UUID.randomUUID()}.jpg" }
        }

        fun Recipe.toDto(): BackupRecipeDto {
            val photoFileName = registerImage(photoUri)
            val stepDtos = steps.map { step ->
                when (step) {
                    is StepContentItem.Text -> BackupStepDto(type = "text", text = step.content)
                    is StepContentItem.Image -> BackupStepDto(type = "image", imageFileName = registerImage(step.url))
                }
            }
            return BackupRecipeDto(
                title = title,
                category = category.name,
                photoFileName = photoFileName,
                cookingMethod = cookingMethod?.name,
                cookingTimeMinutes = cookingTimeMinutes,
                ingredients = ingredients.map { ingredient ->
                    BackupIngredientDto(
                        productName = ingredient.product.name,
                        category = ingredient.product.category.name,
                        unit = ingredient.unit.name,
                        quantity = ingredient.quantity
                    )
                },
                steps = stepDtos
            )
        }

        val payload: BackupPayload = when (type) {
            BackupType.SINGLE_RECIPE -> {
                val recipeId = requireNotNull(singleRecipeId) { "Не указан рецепт для экспорта" }
                val recipe = recipeRepository.getRecipe(recipeId)
                    ?: throw IllegalStateException("Рецепт не найден")
                BackupPayload(
                    scope = BackupScope.SINGLE_RECIPE.name,
                    exportedAt = System.currentTimeMillis(),
                    recipes = listOf(recipe.toDto())
                )
            }

            BackupType.RECIPES_ONLY -> {
                val recipes = recipeRepository.observeRecipes().first()
                BackupPayload(
                    scope = BackupScope.RECIPES_ONLY.name,
                    exportedAt = System.currentTimeMillis(),
                    recipes = recipes.map { it.toDto() }
                )
            }

            BackupType.FULL -> {
                val recipes = recipeRepository.observeRecipes().first()
                val recipeDtos = recipes.map { it.toDto() }

                val fridgeItems = fridgeRepository.observeItems().first()
                val fridgeDtos = fridgeItems.map {
                    BackupFridgeItemDto(
                        productName = it.product.name,
                        category = it.product.category.name,
                        unit = it.unit.name,
                        quantity = it.quantity
                    )
                }

                val shoppingItems = shoppingListRepository.observeItems().first()
                val shoppingDtos = shoppingItems.map {
                    BackupShoppingItemDto(
                        productName = it.product.name,
                        category = it.product.category.name,
                        unit = it.unit.name,
                        quantity = it.quantity,
                        isChecked = it.isChecked
                    )
                }

                val menuEntries = menuRepository.observeWeekMenu().first()
                val menuDtos = menuEntries.map {
                    BackupMenuEntryDto(
                        dayOfWeek = it.dayOfWeek.name,
                        mealType = it.mealType.name,
                        recipeTitle = it.recipeTitle
                    )
                }

                BackupPayload(
                    scope = BackupScope.FULL.name,
                    exportedAt = System.currentTimeMillis(),
                    fridgeItems = fridgeDtos,
                    shoppingItems = shoppingDtos,
                    menuEntries = menuDtos,
                    recipes = recipeDtos
                )
            }
        }

        context.contentResolver.openOutputStream(destinationUri)?.use { rawOut ->
            ZipOutputStream(BufferedOutputStream(rawOut)).use { zip ->
                zip.putNextEntry(ZipEntry("backup.json"))
                zip.write(backupJson.encodeToString(payload).toByteArray())
                zip.closeEntry()

                imageFilesToPack.forEach { (originalUriString, zipFileName) ->
                    runCatching {
                        context.contentResolver.openInputStream(originalUriString.toUri())?.use { input ->
                            zip.putNextEntry(ZipEntry("images/$zipFileName"))
                            input.copyTo(zip)
                            zip.closeEntry()
                        }
                    }
                }
            }
        } ?: throw IllegalStateException("Не удалось открыть файл для записи")

        return BackupResult(
            fridgeItemsCount = payload.fridgeItems.size,
            shoppingItemsCount = payload.shoppingItems.size,
            menuEntriesCount = payload.menuEntries.size,
            recipesCount = payload.recipes.size
        )
    }

    private suspend fun importBackupInternal(sourceUri: Uri): BackupResult {
        var payload: BackupPayload? = null
        val extractedImages = mutableMapOf<String, String>()

        context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
            ZipInputStream(rawIn).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "backup.json" -> {
                            payload = backupJson.decodeFromString<BackupPayload>(zip.readBytes().decodeToString())
                        }
                        entry.name.startsWith("images/") && !entry.isDirectory -> {
                            val fileName = entry.name.removePrefix("images/")
                            val newUri = imageFileManager.persistImageBytes(zip.readBytes())
                            extractedImages[fileName] = newUri
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw IllegalStateException("Не удалось открыть файл резервной копии")

        val data = payload ?: throw IllegalStateException("Файл резервной копии повреждён")

        // Рецепты — всегда добавляются как новые записи; заодно запоминаем
        // title -> id для восстановления записей меню (только для FULL).
        val recipeIdByTitle = mutableMapOf<String, Long>()

        data.recipes.forEach { recipeDto ->
            val ingredients = recipeDto.ingredients.map { ingredientDto ->
                val product = productRepository.findOrCreate(
                    name = ingredientDto.productName,
                    category = Category.valueOf(ingredientDto.category),
                    defaultUnit = MeasureUnit.valueOf(ingredientDto.unit)
                )
                RecipeIngredient(
                    product = product,
                    unit = MeasureUnit.valueOf(ingredientDto.unit),
                    quantity = ingredientDto.quantity
                )
            }

            val steps = recipeDto.steps.mapNotNull { stepDto ->
                when (stepDto.type) {
                    "image" -> extractedImages[stepDto.imageFileName]?.let { StepContentItem.Image(url = it) }
                    else -> StepContentItem.Text(content = stepDto.text ?: "")
                }
            }

            val photoUri = recipeDto.photoFileName?.let { extractedImages[it] }

            val newRecipeId = recipeRepository.addRecipe(
                Recipe(
                    title = recipeDto.title,
                    category = RecipeCategory.valueOf(recipeDto.category),
                    photoUri = photoUri,
                    cookingMethod = recipeDto.cookingMethod?.let { name ->
                        CookingMethod.entries.firstOrNull { it.name == name }
                    },
                    cookingTimeMinutes = recipeDto.cookingTimeMinutes,
                    ingredients = ingredients,
                    steps = steps
                )
            )
            recipeIdByTitle[recipeDto.title] = newRecipeId
        }

        // Холодильник — суммируем с уже имеющимся количеством
        val currentFridgeItems = fridgeRepository.observeItems().first().toMutableList()
        data.fridgeItems.forEach { dto ->
            val product = productRepository.findOrCreate(
                name = dto.productName,
                category = Category.valueOf(dto.category),
                defaultUnit = MeasureUnit.valueOf(dto.unit)
            )
            val unit = MeasureUnit.valueOf(dto.unit)
            val existing = currentFridgeItems.firstOrNull { it.product.id == product.id && it.unit == unit }
            if (existing != null) {
                val updated = existing.copy(quantity = existing.quantity + dto.quantity)
                fridgeRepository.updateItem(updated)
                currentFridgeItems[currentFridgeItems.indexOf(existing)] = updated
            } else {
                val newId = fridgeRepository.addItem(FridgeItem(product = product, unit = unit, quantity = dto.quantity))
                currentFridgeItems.add(FridgeItem(id = newId, product = product, unit = unit, quantity = dto.quantity))
            }
        }

        // Список покупок — суммируем с непроверенными позициями того же продукта
        val currentShoppingItems = shoppingListRepository.observeItems().first().toMutableList()
        data.shoppingItems.forEach { dto ->
            val product = productRepository.findOrCreate(
                name = dto.productName,
                category = Category.valueOf(dto.category),
                defaultUnit = MeasureUnit.valueOf(dto.unit)
            )
            val unit = MeasureUnit.valueOf(dto.unit)
            val existing = currentShoppingItems.firstOrNull {
                it.product.id == product.id && it.unit == unit && it.isChecked == dto.isChecked
            }
            if (existing != null) {
                val updated = existing.copy(quantity = existing.quantity + dto.quantity)
                shoppingListRepository.updateItem(updated)
                currentShoppingItems[currentShoppingItems.indexOf(existing)] = updated
            } else {
                val newId = shoppingListRepository.addItem(
                    ShoppingListItem(product = product, unit = unit, quantity = dto.quantity, isChecked = dto.isChecked)
                )
                currentShoppingItems.add(
                    ShoppingListItem(id = newId, product = product, unit = unit, quantity = dto.quantity, isChecked = dto.isChecked)
                )
            }
        }

        // Меню — восстанавливаем только записи, для которых рецепт найден среди только что импортированных
        data.menuEntries.forEach { dto ->
            val recipeId = recipeIdByTitle[dto.recipeTitle] ?: return@forEach
            menuRepository.addEntry(
                MenuEntry(
                    dayOfWeek = DayOfWeek.valueOf(dto.dayOfWeek),
                    mealType = MealType.valueOf(dto.mealType),
                    recipeId = recipeId
                )
            )
        }

        return BackupResult(
            fridgeItemsCount = data.fridgeItems.size,
            shoppingItemsCount = data.shoppingItems.size,
            menuEntriesCount = data.menuEntries.size,
            recipesCount = data.recipes.size
        )
    }
}