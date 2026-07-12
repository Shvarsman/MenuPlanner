package com.shvarsman.menuplanner.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.shvarsman.menuplanner.data.backup.BackupFridgeItemDto
import com.shvarsman.menuplanner.data.backup.BackupIngredientDto
import com.shvarsman.menuplanner.data.backup.BackupPayload
import com.shvarsman.menuplanner.data.backup.BackupRecipeDto
import com.shvarsman.menuplanner.data.backup.BackupStepDto
import com.shvarsman.menuplanner.data.local.ImageFileManager
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.BackupResult
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private val backupJson = Json { prettyPrint = true; ignoreUnknownKeys = true }

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val fridgeRepository: FridgeRepository,
    private val imageFileManager: ImageFileManager
) : BackupRepository {

    override suspend fun exportBackup(destinationUri: Uri): BackupResult {
        val fridgeItems = fridgeRepository.observeItems().first()
        val recipes = recipeRepository.observeRecipes().first()

        val imageFilesToPack = mutableMapOf<String, String>()
        fun registerImage(uriString: String?): String? {
            if (uriString == null) return null
            return imageFilesToPack.getOrPut(uriString) { "img_${UUID.randomUUID()}.jpg" }
        }

        val recipeDtos = recipes.map { recipe ->
            val photoFileName = registerImage(recipe.photoUri)
            val stepDtos = recipe.steps.map { step ->
                when (step) {
                    is StepContentItem.Text -> BackupStepDto(type = "text", text = step.content)
                    is StepContentItem.Image -> BackupStepDto(
                        type = "image",
                        imageFileName = registerImage(step.url)
                    )
                }
            }
            BackupRecipeDto(
                title = recipe.title,
                category = recipe.category.name,
                photoFileName = photoFileName,
                cookingMethod = recipe.cookingMethod?.name,
                cookingTimeMinutes = recipe.cookingTimeMinutes,
                ingredients = recipe.ingredients.map { ingredient ->
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

        val fridgeItemDtos = fridgeItems.map {
            BackupFridgeItemDto(
                productName = it.product.name,
                category = it.product.category.name,
                unit = it.unit.name,
                quantity = it.quantity
            )
        }

        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            fridgeItems = fridgeItemDtos,
            recipes = recipeDtos
        )

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

        return BackupResult(fridgeItemsCount = fridgeItemDtos.size, recipesCount = recipeDtos.size)
    }

    override suspend fun importBackup(sourceUri: Uri): BackupResult {
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

        // Восстанавливаем количество продуктов в холодильнике, суммируя с уже имеющимся
        val currentFridgeItems = fridgeRepository.observeItems().first()
        data.fridgeItems.forEach { dto ->
            val product = productRepository.findOrCreate(
                name = dto.productName,
                category = Category.valueOf(dto.category),
                defaultUnit = MeasureUnit.valueOf(dto.unit)
            )
            val unit = MeasureUnit.valueOf(dto.unit)
            val existing =
                currentFridgeItems.firstOrNull { it.product.id == product.id && it.unit == unit }
            if (existing != null) {
                fridgeRepository.updateItem(existing.copy(quantity = existing.quantity + dto.quantity))
            } else {
                fridgeRepository.addItem(
                    FridgeItem(
                        product = product,
                        unit = unit,
                        quantity = dto.quantity
                    )
                )
            }
        }

        // Рецепты всегда добавляются как новые записи
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
                    "image" -> extractedImages[stepDto.imageFileName]?.let {
                        StepContentItem.Image(
                            url = it
                        )
                    }

                    else -> StepContentItem.Text(content = stepDto.text ?: "")
                }
            }

            val photoUri = recipeDto.photoFileName?.let { extractedImages[it] }

            recipeRepository.addRecipe(
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
        }

        return BackupResult(
            fridgeItemsCount = data.fridgeItems.size,
            recipesCount = data.recipes.size
        )
    }
}