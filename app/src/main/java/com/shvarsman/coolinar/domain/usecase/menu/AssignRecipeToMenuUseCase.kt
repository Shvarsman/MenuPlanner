package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.ReservedKey
import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.TransactionRunner
import com.shvarsman.coolinar.domain.usecase.shoppinglist.AddToShoppingListUseCase
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class AssignRecipeToMenuUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val fridgeRepository: FridgeRepository,
    private val addToShoppingList: AddToShoppingListUseCase,
    private val getReservedQuantities: GetReservedQuantitiesUseCase,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(
        weekStart: LocalDate,
        day: DayOfWeek,
        mealType: MealType,
        recipe: Recipe
    ): String =
        transactionRunner.runInTransaction {
            val reserved = getReservedQuantities(weekStart)
            val fridgeSnapshot = fridgeRepository.observeItems().first()

            val entryId = menuRepository.addEntry(
                MenuEntry(
                    weekStartDate = weekStart,
                    dayOfWeek = day,
                    mealType = mealType,
                    recipeId = recipe.id
                )
            )

            recipe.ingredients.forEach { ingredient ->
                if (ingredient.product.isAlwaysAvailable) return@forEach

                if (ingredient.product.isToTaste) {
                    val hasAny = fridgeSnapshot.any { it.product.id == ingredient.product.id }
                    if (!hasAny) {
                        addToShoppingList(ingredient.product, ingredient.product.defaultUnit, 1.0)
                    }
                    return@forEach
                }

                val fridgeQty = fridgeSnapshot
                    .filter { it.product.id == ingredient.product.id }
                    .sumOf { UnitConversion.convert(it.quantity, it.unit, ingredient.unit) ?: 0.0 }

                val canonical = UnitConversion.canonicalUnit(ingredient.unit)
                val reservedAmount = reserved[ReservedKey(ingredient.product.id, canonical)]
                val reservedQty = reservedAmount
                    ?.let { UnitConversion.convert(it.amount, it.unit, ingredient.unit) } ?: 0.0

                val trulyAvailable = (fridgeQty - reservedQty).coerceAtLeast(0.0)

                if (trulyAvailable < ingredient.quantity) {
                    val shortage = ingredient.quantity - trulyAvailable
                    addToShoppingList(ingredient.product, ingredient.unit, shortage)
                }
            }

            entryId
        }
}