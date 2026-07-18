package com.shvarsman.menuplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeIngredientEntity
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
    val id: Long,
    val title: String,
    val category: com.shvarsman.menuplanner.domain.model.RecipeCategory,
    val photoUri: String?,
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
            recipes.stepCount,
            (SELECT COUNT(*) FROM recipe_ingredients WHERE recipeId = recipes.id) AS ingredientCount
        FROM recipes
        ORDER BY recipes.title ASC
        """
    )
    fun observeSummaries(): Flow<List<RecipeSummaryRow>>

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun observeAllWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getByIdWithIngredients(id: Long): RecipeWithIngredients?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    @Transaction
    suspend fun upsertRecipeWithIngredients(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>
    ): Long {
        val recipeId = insertRecipe(recipe)
        deleteIngredientsForRecipe(recipeId)
        insertIngredients(ingredients.map { it.copy(recipeId = recipeId) })
        return recipeId
    }
}