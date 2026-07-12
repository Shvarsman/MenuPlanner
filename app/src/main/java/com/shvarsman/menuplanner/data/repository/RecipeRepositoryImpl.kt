package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.RecipeDao
import com.shvarsman.menuplanner.data.local.dao.RecipeIngredientWithProduct
import com.shvarsman.menuplanner.data.local.dao.RecipeWithIngredients
import com.shvarsman.menuplanner.data.local.entity.RecipeEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeIngredientEntity
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val dao: RecipeDao
) : RecipeRepository {

    override fun observeRecipes(): Flow<List<Recipe>> =
        dao.observeAllWithIngredients().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getRecipe(id: Long): Recipe? = dao.getByIdWithIngredients(id)?.toDomain()

    override suspend fun addRecipe(recipe: Recipe): Long {
        val recipeId = dao.insertRecipe(recipe.toEntity())
        dao.insertIngredients(recipe.ingredients.map { it.toEntity(recipeId) })
        return recipeId
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        dao.updateRecipe(recipe.toEntity())
        dao.deleteIngredientsForRecipe(recipe.id)
        dao.insertIngredients(recipe.ingredients.map { it.toEntity(recipe.id) })
    }

    override suspend fun deleteRecipe(id: Long) = dao.deleteRecipe(id)
}

private fun RecipeWithIngredients.toDomain() = Recipe(
    id = recipe.id,
    title = recipe.title,
    category = recipe.category,
    photoUri = recipe.photoUri,
    cookingMethod = recipe.cookingMethod,
    cookingTimeMinutes = recipe.cookingTimeMinutes,
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
        isDefault = product.isDefault
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
    steps = steps
)

private fun RecipeIngredient.toEntity(recipeId: Long) = RecipeIngredientEntity(
    id = id,
    recipeId = recipeId,
    productId = product.id,
    unit = unit,
    quantity = quantity
)