package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.RecipeDao
import com.shvarsman.coolinar.data.local.dao.RecipeIngredientWithProduct
import com.shvarsman.coolinar.data.local.dao.RecipeSummaryRow
import com.shvarsman.coolinar.data.local.dao.RecipeWithIngredients
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import com.shvarsman.coolinar.data.local.entity.RecipeIngredientEntity
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.model.StepContentItem
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val dao: RecipeDao
) : RecipeRepository {

    override fun observeRecipeSummaries(): Flow<List<RecipeSummary>> =
        dao.observeSummaries()
            .map { list -> list.map { it.toSummary() } }
            .flowOn(Dispatchers.Default)

    override fun observeRecipes(): Flow<List<Recipe>> =
        dao.observeAllWithIngredients()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    override suspend fun getRecipe(id: Long): Recipe? = dao.getByIdWithIngredients(id)?.toDomain()

    override suspend fun addRecipe(recipe: Recipe): Long =
        dao.upsertRecipeWithIngredients(
            recipe.toEntity(),
            recipe.ingredients.map { it.toEntity(0) })

    override suspend fun updateRecipe(recipe: Recipe) {
        dao.upsertRecipeWithIngredients(
            recipe.toEntity(),
            recipe.ingredients.map { it.toEntity(recipe.id) })
    }

    override suspend fun deleteRecipe(id: Long) = dao.deleteRecipe(id)
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

private fun RecipeIngredient.toEntity(recipeId: Long) = RecipeIngredientEntity(
    id = id,
    recipeId = recipeId,
    productId = product.id,
    unit = unit,
    quantity = quantity
)
