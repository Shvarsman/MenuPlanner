package com.shvarsman.coolinar.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.shvarsman.coolinar.data.backup.BackupFridgeItemDto
import com.shvarsman.coolinar.data.backup.BackupIngredientDto
import com.shvarsman.coolinar.data.backup.BackupMenuEntryDto
import com.shvarsman.coolinar.data.backup.BackupPayload
import com.shvarsman.coolinar.data.backup.BackupProductRefDto
import com.shvarsman.coolinar.data.backup.BackupRecipeDto
import com.shvarsman.coolinar.data.backup.BackupScope
import com.shvarsman.coolinar.data.backup.BackupShoppingItemDto
import com.shvarsman.coolinar.data.backup.BackupStepDto
import com.shvarsman.coolinar.data.local.ImageFileManager
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.domain.model.StepContentItem
import com.shvarsman.coolinar.domain.repository.BackupRepository
import com.shvarsman.coolinar.domain.repository.BackupResult
import com.shvarsman.coolinar.domain.repository.BackupType
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.ProductRepository
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
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

    override suspend fun exportBackup(
        destinationUri: Uri,
        type: BackupType,
        singleRecipeId: String?
    ): BackupResult =
        withContext(Dispatchers.IO) {
            exportBackupInternal(destinationUri, type, singleRecipeId)
        }

    override suspend fun importBackup(sourceUri: Uri): BackupResult =
        withContext(Dispatchers.IO) {
            importBackupInternal(sourceUri)
        }

    private suspend fun exportBackupInternal(
        destinationUri: Uri,
        type: BackupType,
        singleRecipeId: String?
    ): BackupResult {
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
                    is StepContentItem.Image -> BackupStepDto(
                        type = "image",
                        imageFileName = registerImage(step.url)
                    )

                    is StepContentItem.Timer -> BackupStepDto(
                        type = "timer",
                        minutes = step.minutes
                    )
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
                        product = ingredient.product.toBackupRef(ingredient.unit),
                        quantity = ingredient.quantity
                    )
                },
                steps = stepDtos,
                difficulty = difficulty.name,
                description = description,
                isFavorite = isFavorite
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
                        product = it.product.toBackupRef(it.unit),
                        quantity = it.quantity,
                        expirationDate = it.expirationDate?.toString(),
                        isFavorite = it.isFavorite
                    )
                }

                val shoppingItems = shoppingListRepository.observeItems().first()
                val shoppingDtos = shoppingItems.map {
                    BackupShoppingItemDto(
                        product = it.product.toBackupRef(it.unit),
                        quantity = it.quantity,
                        isChecked = it.isChecked,
                        expirationDate = it.expirationDate?.toString()
                    )
                }

                val thisWeekStart =
                    LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val menuEntries = (0..1).flatMap { offset ->
                    menuRepository.observeWeekMenu(thisWeekStart.plusWeeks(offset.toLong())).first()
                }
                val menuDtos = menuEntries.map {
                    BackupMenuEntryDto(
                        weekOffset = if (it.weekStartDate == thisWeekStart) 0 else 1,
                        dayOfWeek = it.dayOfWeek.name,
                        mealType = it.mealType.name,
                        recipeTitle = it.recipeTitle,
                        createdAt = it.createdAt
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
                        context.contentResolver.openInputStream(originalUriString.toUri())
                            ?.use { input ->
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
                            payload = backupJson.decodeFromString<BackupPayload>(
                                zip.readBytes().decodeToString()
                            )
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
        val recipeIdByTitle = mutableMapOf<String, String>()

        data.recipes.forEach { recipeDto ->
            val ingredients = recipeDto.ingredients.map { ingredientDto ->
                val product = productRepository.findOrCreate(
                    name = ingredientDto.product.name,
                    category = Category.valueOf(ingredientDto.product.category),
                    defaultUnit = MeasureUnit.valueOf(ingredientDto.product.unit),
                    isToTaste = ingredientDto.product.isToTaste,
                    isAlwaysAvailable = ingredientDto.product.isAlwaysAvailable
                )
                RecipeIngredient(
                    product = product,
                    unit = MeasureUnit.valueOf(ingredientDto.product.unit),
                    quantity = ingredientDto.quantity
                )
            }

            val steps = recipeDto.steps.mapNotNull { stepDto ->
                when (stepDto.type) {
                    "image" -> extractedImages[stepDto.imageFileName]?.let {
                        StepContentItem.Image(
                            url = it
                        )
                    }

                    "timer" -> StepContentItem.Timer(minutes = stepDto.minutes ?: 5)
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
                    steps = steps,
                    difficulty = RecipeDifficulty.entries.firstOrNull { it.name == recipeDto.difficulty }
                        ?: RecipeDifficulty.EASY,
                    description = recipeDto.description,
                    isFavorite = recipeDto.isFavorite
                )
            )
            recipeIdByTitle[recipeDto.title] = newRecipeId
        }

        // Холодильник — суммируем с уже имеющимся количеством
        val currentFridgeItems = fridgeRepository.observeItems().first().toMutableList()
        data.fridgeItems.forEach { dto ->
            val product = productRepository.findOrCreate(
                name = dto.product.name,
                category = Category.valueOf(dto.product.category),
                defaultUnit = MeasureUnit.valueOf(dto.product.unit),
                isToTaste = dto.product.isToTaste,
                isAlwaysAvailable = dto.product.isAlwaysAvailable
            )
            val unit = MeasureUnit.valueOf(dto.product.unit)
            val expirationDate = dto.expirationDate?.let { LocalDate.parse(it) }
            val existing =
                currentFridgeItems.firstOrNull { it.product.id == product.id && it.unit == unit }
            if (existing != null) {
                val updated = existing.copy(
                    quantity = existing.quantity + dto.quantity,
                    expirationDate = expirationDate ?: existing.expirationDate,
                    isFavorite = existing.isFavorite || dto.isFavorite
                )
                fridgeRepository.updateItem(updated)
                currentFridgeItems[currentFridgeItems.indexOf(existing)] = updated
            } else {
                val newId = fridgeRepository.addItem(
                    FridgeItem(
                        product = product,
                        unit = unit,
                        quantity = dto.quantity,
                        expirationDate = expirationDate,
                        isFavorite = dto.isFavorite
                    )
                )
                currentFridgeItems.add(
                    FridgeItem(
                        id = newId,
                        product = product,
                        unit = unit,
                        quantity = dto.quantity,
                        expirationDate = expirationDate,
                        isFavorite = dto.isFavorite
                    )
                )
            }
        }

        // Список покупок — суммируем с непроверенными позициями того же продукта
        val currentShoppingItems = shoppingListRepository.observeItems().first().toMutableList()
        data.shoppingItems.forEach { dto ->
            val product = productRepository.findOrCreate(
                name = dto.product.name,
                category = Category.valueOf(dto.product.category),
                defaultUnit = MeasureUnit.valueOf(dto.product.unit),
                isToTaste = dto.product.isToTaste,
                isAlwaysAvailable = dto.product.isAlwaysAvailable
            )
            val unit = MeasureUnit.valueOf(dto.product.unit)
            val expirationDate = dto.expirationDate?.let { LocalDate.parse(it) }
            val existing = currentShoppingItems.firstOrNull {
                it.product.id == product.id && it.unit == unit && it.isChecked == dto.isChecked
            }
            if (existing != null) {
                val updated = existing.copy(
                    quantity = existing.quantity + dto.quantity,
                    expirationDate = expirationDate ?: existing.expirationDate
                )
                shoppingListRepository.updateItem(updated)
                currentShoppingItems[currentShoppingItems.indexOf(existing)] = updated
            } else {
                val newId = shoppingListRepository.addItem(
                    ShoppingListItem(
                        product = product,
                        unit = unit,
                        quantity = dto.quantity,
                        isChecked = dto.isChecked,
                        expirationDate = expirationDate
                    )
                )
                currentShoppingItems.add(
                    ShoppingListItem(
                        id = newId,
                        product = product,
                        unit = unit,
                        quantity = dto.quantity,
                        isChecked = dto.isChecked,
                        expirationDate = expirationDate
                    )
                )
            }
        }

        // Меню — восстанавливаем только записи, для которых рецепт найден среди только что импортированных
        val importWeekStart =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        data.menuEntries.forEach { dto ->
            val recipeId = recipeIdByTitle[dto.recipeTitle] ?: return@forEach
            menuRepository.addEntry(
                MenuEntry(
                    weekStartDate = importWeekStart.plusWeeks(dto.weekOffset.toLong()),
                    dayOfWeek = DayOfWeek.valueOf(dto.dayOfWeek),
                    mealType = MealType.valueOf(dto.mealType),
                    recipeId = recipeId,
                    createdAt = dto.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
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

private fun Product.toBackupRef(unit: MeasureUnit) = BackupProductRefDto(
    name = name,
    category = category.name,
    unit = unit.name,
    isToTaste = isToTaste,
    isAlwaysAvailable = isAlwaysAvailable
)