package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.converter.Converters
import com.shvarsman.coolinar.data.local.dao.RecipeDao
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import com.shvarsman.coolinar.data.local.entity.RecipeIngredientEntity
import com.shvarsman.coolinar.data.remote.sync.dto.RecipeDto
import com.shvarsman.coolinar.data.remote.sync.dto.RecipeIngredientDto
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeSyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: RecipeDao,
    syncScope: SyncScope
) : FirestoreSyncEngine<RecipeEntity, RecipeDto>(
    firestore,
    "recipes",
    RecipeDto::class.java,
    syncScope
) {
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

        return RecipeDto(
            title = title,
            category = category.name,
            photoUri = photoUri,
            cookingMethod = cookingMethod?.name,
            cookingTimeMinutes = cookingTimeMinutes,
            difficulty = difficulty.name,
            description = description,
            stepsSerialized = converters.fromStepContentList(steps),
            stepCount = stepCount,
            favorite = isFavorite,
            ingredients = ingredientDtos,
            updatedAt = updatedAt,
            deleted = isDeleted
        )
    }

    override fun RecipeDto.toLocal(id: String) = RecipeEntity(
        id = id,
        title = title,
        category = RecipeCategory.entries.first { it.name == category },
        // photoUri здесь — либо file://, если с исходного устройства ещё не
        // успело загрузиться в Storage, либо https://, если уже загрузилось.
        // Coil грузит оба варианта одинаково, без специальной обработки.
        photoUri = photoUri,
        cookingMethod = cookingMethod?.let { name -> CookingMethod.entries.firstOrNull { it.name == name } },
        cookingTimeMinutes = cookingTimeMinutes,
        difficulty = RecipeDifficulty.entries.firstOrNull { it.name == difficulty } ?: RecipeDifficulty.EASY,
        description = description,
        steps = converters.toStepContentList(stepsSerialized),
        stepCount = stepCount,
        isFavorite = favorite,
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<RecipeEntity, RecipeDto>>) {
        items.forEach { (entity, dto) ->
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

    override suspend fun getAllLocalIncludingDeleted(): List<RecipeEntity> =
        dao.getAllWithIngredientsIncludingDeleted().map { it.recipe }

    override suspend fun getLocalById(id: String): RecipeEntity? =
        dao.getByIdWithIngredientsIncludingDeleted(id)?.recipe
}