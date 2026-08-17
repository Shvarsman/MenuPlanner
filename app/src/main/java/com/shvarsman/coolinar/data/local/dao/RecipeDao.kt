package com.shvarsman.coolinar.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import com.shvarsman.coolinar.data.local.entity.RecipeIngredientEntity
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import kotlinx.coroutines.flow.Flow

data class RecipeIngredientWithProduct(
    @Embedded val ingredient: RecipeIngredientEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        entity = RecipeIngredientEntity::class,
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<RecipeIngredientWithProduct>
)

data class RecipeSummaryRow(
    val id: String,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val cookingMethod: CookingMethod?,
    val cookingTimeMinutes: Int?,
    val difficulty: RecipeDifficulty,
    val isFavorite: Boolean,
    val ingredientCount: Int,
    val stepCount: Int
)

@Dao
interface RecipeDao {
    @Query(
        """
    SELECT
        recipes.id,
        recipes.title,
        recipes.category,
        recipes.photoUri,
        recipes.cookingMethod,
        recipes.cookingTimeMinutes,
        recipes.difficulty,
        recipes.isFavorite,
        recipes.stepCount,
        (SELECT COUNT(*) FROM recipe_ingredients WHERE recipeId = recipes.id) AS ingredientCount
    FROM recipes
    WHERE recipes.isDeleted = 0
    ORDER BY recipes.title ASC
    """
    )
    fun observeSummaries(): Flow<List<RecipeSummaryRow>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY title ASC")
    fun observeAllWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id AND isDeleted = 0")
    suspend fun getByIdWithIngredients(id: String): RecipeWithIngredients?

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getByIdWithIngredientsIncludingDeleted(id: String): RecipeWithIngredients?

    @Transaction
    @Query("SELECT * FROM recipes")
    suspend fun getAllWithIngredientsIncludingDeleted(): List<RecipeWithIngredients>

    @Upsert
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteRecipe(id: String, updatedAt: Long)

    @Query("UPDATE recipes SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreRecipe(id: String, updatedAt: Long)

    @Query("UPDATE recipes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)

    @Query("DELETE FROM recipes WHERE id IN (:ids)")
    suspend fun deleteByIdsHard(ids: List<String>)

    @Query("SELECT id FROM recipes")
    suspend fun getAllIdsIncludingDeleted(): List<String>

    @Transaction
    suspend fun upsertRecipeWithIngredients(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>
    ) {
        insertRecipe(recipe)
        deleteIngredientsForRecipe(recipe.id)
        insertIngredients(ingredients)
    }
}