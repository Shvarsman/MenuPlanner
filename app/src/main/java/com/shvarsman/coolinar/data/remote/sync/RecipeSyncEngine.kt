package com.shvarsman.coolinar.data.remote.sync

import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.ImageFileManager
import com.shvarsman.coolinar.data.local.converter.Converters
import com.shvarsman.coolinar.data.local.dao.RecipeDao
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import com.shvarsman.coolinar.data.local.entity.RecipeIngredientEntity
import com.shvarsman.coolinar.data.remote.sync.dto.RecipeDto
import com.shvarsman.coolinar.data.remote.sync.dto.RecipeIngredientDto
import com.shvarsman.coolinar.data.remote.sync.dto.RecipeStepImageDto
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import com.shvarsman.coolinar.domain.model.StepContentItem
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeSyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: RecipeDao,
    private val imageFileManager: ImageFileManager
) : FirestoreSyncEngine<RecipeEntity, RecipeDto>(firestore, "recipes", RecipeDto::class.java) {

    private val converters = Converters()

    override suspend fun RecipeEntity.toDto(): RecipeDto {
        val withIngredients = dao.getByIdWithIngredientsIncludingDeleted(id)
        val ingredientDtos = withIngredients?.ingredients?.map { row ->
            RecipeIngredientDto(
                id = row.ingredient.id,
                productId = row.ingredient.productId,
                unit = row.ingredient.unit.name,
                quantity = row.ingredient.quantity
            )
        } ?: emptyList()

        var remainingBudget = DOCUMENT_BYTE_BUDGET

        val coverBase64 = photoUri
            ?.takeIf { it.isLocalFile() }
            ?.let { uri -> imageFileManager.readCompressedBytes(uri, COVER_TARGET_BYTES) }
            ?.also { remainingBudget -= it.size }
            ?.let { Base64.encodeToString(it, Base64.NO_WRAP) }

        val stepImageDtos = mutableListOf<RecipeStepImageDto>()
        steps.forEachIndexed { index, step ->
            if (step !is StepContentItem.Image) return@forEachIndexed
            if (!step.url.isLocalFile()) return@forEachIndexed
            if (remainingBudget < STEP_IMAGE_TARGET_BYTES) return@forEachIndexed // бюджет документа исчерпан — остальные фото шагов просто не улетят в облако

            val bytes = imageFileManager.readCompressedBytes(step.url, STEP_IMAGE_TARGET_BYTES)
                ?: return@forEachIndexed
            remainingBudget -= bytes.size
            stepImageDtos += RecipeStepImageDto(
                stepIndex = index,
                imageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            )
        }

        return RecipeDto(
            title = title,
            category = category.name,
            coverPhotoBase64 = coverBase64,
            cookingMethod = cookingMethod?.name,
            cookingTimeMinutes = cookingTimeMinutes,
            difficulty = difficulty.name,
            description = description,
            stepsSerialized = converters.fromStepContentList(steps),
            stepCount = stepCount,
            stepImages = stepImageDtos,
            ingredients = ingredientDtos,
            updatedAt = updatedAt,
            deleted = isDeleted
        )
    }

    override fun RecipeDto.toLocal(id: String) = RecipeEntity(
        id = id,
        title = title,
        category = RecipeCategory.entries.first { it.name == category },
        // photoUri временно оставляем как было в облаке (пусто, если фото не влезло
        // в бюджет документа) — upsertLocal ниже подменит его на реальный локальный файл
        photoUri = null,
        cookingMethod = cookingMethod?.let { name -> CookingMethod.entries.firstOrNull { it.name == name } },
        cookingTimeMinutes = cookingTimeMinutes,
        difficulty = RecipeDifficulty.entries.firstOrNull { it.name == difficulty }
            ?: RecipeDifficulty.EASY,
        description = description,
        steps = converters.toStepContentList(stepsSerialized),
        stepCount = stepCount,
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<RecipeEntity, RecipeDto>>) {
        items.forEach { (baseEntity, dto) ->
            val previous = dao.getByIdWithIngredientsIncludingDeleted(baseEntity.id)

            // Распаковываем обложку (если пришла) в новый локальный файл
            val newPhotoUri = dto.coverPhotoBase64
                ?.let { runCatching { Base64.decode(it, Base64.NO_WRAP) }.getOrNull() }
                ?.let { bytes -> imageFileManager.persistImageBytes(bytes) }
                ?: previous?.recipe?.photoUri // фото не пришло в этом апдейте — сохраняем то, что уже было локально
            // Распаковываем фото шагов по их индексу в списке
            val newSteps = dto.stepsSerialized
                .let { converters.toStepContentList(it) }
                .toMutableList()
            dto.stepImages.forEach { stepImageDto ->
                val decoded = runCatching {
                    Base64.decode(
                        stepImageDto.imageBase64,
                        Base64.NO_WRAP
                    )
                }.getOrNull()
                    ?: return@forEach
                val localUri = imageFileManager.persistImageBytes(decoded)
                val index = stepImageDto.stepIndex
                if (index in newSteps.indices && newSteps[index] is StepContentItem.Image) {
                    newSteps[index] = StepContentItem.Image(localUri)
                }
            }

            val entity = baseEntity.copy(photoUri = newPhotoUri, steps = newSteps)

            // Чистим файлы, которые больше никем не используются после подмены —
            // иначе локальное хранилище будет бесконтрольно расти с каждым апдейтом
            cleanupOrphanedFiles(previous?.recipe, entity)

            val ingredients = dto.ingredients.map { ing ->
                RecipeIngredientEntity(
                    id = ing.id.ifBlank { UUID.randomUUID().toString() },
                    recipeId = entity.id,
                    productId = ing.productId,
                    unit = MeasureUnit.entries.first { it.name == ing.unit },
                    quantity = ing.quantity
                )
            }
            dao.upsertRecipeWithIngredients(entity, ingredients)
        }
    }

    private suspend fun cleanupOrphanedFiles(old: RecipeEntity?, new: RecipeEntity) {
        if (old == null) return
        val oldUrls = buildSet {
            old.photoUri?.let { add(it) }
            old.steps.forEach { if (it is StepContentItem.Image) add(it.url) }
        }
        val newUrls = buildSet {
            new.photoUri?.let { add(it) }
            new.steps.forEach { if (it is StepContentItem.Image) add(it.url) }
        }
        (oldUrls - newUrls).filter { it.isLocalFile() }.forEach { imageFileManager.deleteImage(it) }
    }

    override suspend fun getAllLocalIncludingDeleted(): List<RecipeEntity> =
        dao.getAllWithIngredientsIncludingDeleted().map { it.recipe }

    companion object {
        /** Оставляем запас под остальные поля документа (заголовок, описание,
         * шаги текстом, ингредиенты) от жёсткого лимита Firestore в 1 МиБ. */
        private const val DOCUMENT_BYTE_BUDGET = 850_000
        private const val COVER_TARGET_BYTES = 150_000
        private const val STEP_IMAGE_TARGET_BYTES = 60_000
    }
}

private fun String.isLocalFile(): Boolean = startsWith("file://")