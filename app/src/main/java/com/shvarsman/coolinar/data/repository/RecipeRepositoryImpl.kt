package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.RecipeDao
import com.shvarsman.coolinar.data.local.dao.RecipeIngredientWithProduct
import com.shvarsman.coolinar.data.local.dao.RecipeSummaryRow
import com.shvarsman.coolinar.data.local.dao.RecipeWithIngredients
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import com.shvarsman.coolinar.data.local.entity.RecipeIngredientEntity
import com.shvarsman.coolinar.data.remote.sync.RecipeSyncEngine
import com.shvarsman.coolinar.data.remote.sync.SyncScope
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.model.StepContentItem
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val dao: RecipeDao,
    private val syncEngine: RecipeSyncEngine,
    private val authRepository: AuthRepository,
    private val syncScope: SyncScope
) : RecipeRepository {

    override fun observeRecipeSummaries(): Flow<List<RecipeSummary>> =
        dao.observeSummaries()
            .map { list -> list.map { it.toSummary() } }
            .flowOn(Dispatchers.Default)

    override fun observeRecipes(): Flow<List<Recipe>> =
        dao.observeAllWithIngredients()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    override suspend fun getRecipe(id: String): Recipe? = dao.getByIdWithIngredients(id)?.toDomain()

    override suspend fun addRecipe(recipe: Recipe): String {
        val recipeId = recipe.id.ifBlank { UUID.randomUUID().toString() }
        val ingredientsWithIds = recipe.ingredients.map {
            it.toEntity(recipeId).copy(id = it.id.ifBlank { UUID.randomUUID().toString() })
        }
        val entity = recipe.toEntity().copy(id = recipeId, updatedAt = System.currentTimeMillis())
        dao.upsertRecipeWithIngredients(entity, ingredientsWithIds)
        pushIfSignedIn(entity)
        return recipeId
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val ingredientsWithIds = recipe.ingredients.map {
            it.toEntity(recipe.id).copy(id = it.id.ifBlank { UUID.randomUUID().toString() })
        }
        val entity = recipe.toEntity().copy(updatedAt = System.currentTimeMillis())
        dao.upsertRecipeWithIngredients(entity, ingredientsWithIds)
        pushIfSignedIn(entity)
    }

    override suspend fun deleteRecipe(id: String) {
        val now = System.currentTimeMillis()
        dao.softDeleteRecipe(id, now)
        dao.getByIdWithIngredientsIncludingDeleted(id)?.recipe?.let { pushIfSignedIn(it) }
    }

    private fun pushIfSignedIn(entity: RecipeEntity) {
        val uid = authRepository.currentUserId ?: return
        syncScope.scope.launch {
            runCatching { syncEngine.push(uid, entity) }
                .onFailure { e -> android.util.Log.e("FirestoreSync", "push failed for recipe ${entity.id}", e) }
        }
    }
}

private fun RecipeSummaryRow.toSummary() = RecipeSummary(
    id = id,
    title = title,
    category = category,
    photoUri = photoUri,
    difficulty = difficulty,
    ingredientCount = ingredientCount,
    stepCount = stepCount
)

private fun RecipeWithIngredients.toDomain() = Recipe(
    id = recipe.id,
    title = recipe.title,
    category = recipe.category,
    photoUri = recipe.photoUri,
    cookingMethod = recipe.cookingMethod,
    cookingTimeMinutes = recipe.cookingTimeMinutes,
    difficulty = recipe.difficulty,
    description = recipe.description,
    steps = recipe.steps,
    ingredients = ingredients.map { it.toDomain() }
)

private fun RecipeIngredientWithProduct.toDomain() = RecipeIngredient(
    id = ingredient.id,
    product = Product(
        id = product.id,
        name = product.name,
        category = product.category,
        defaultUnit = product.defaultUnit,
        iconKey = product.iconKey,
        isDefault = product.isDefault,
        isToTaste = product.isToTaste,
        isAlwaysAvailable = product.isAlwaysAvailable
    ),
    unit = ingredient.unit,
    quantity = ingredient.quantity
)

private fun Recipe.toEntity() = RecipeEntity(
    id = id,
    title = title,
    category = category,
    photoUri = photoUri,
    cookingMethod = cookingMethod,
    cookingTimeMinutes = cookingTimeMinutes,
    difficulty = difficulty,
    description = description,
    steps = steps,
    stepCount = steps.count { it is StepContentItem.Text && it.content.isNotBlank() }
)

private fun RecipeIngredient.toEntity(recipeId: String) = RecipeIngredientEntity(
    id = id,
    recipeId = recipeId,
    productId = product.id,
    unit = unit,
    quantity = quantity
)
