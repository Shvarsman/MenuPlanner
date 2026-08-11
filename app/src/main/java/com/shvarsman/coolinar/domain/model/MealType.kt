package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R

enum class MealType(@StringRes val labelRes: Int) {
    BREAKFAST(R.string.meal_type_breakfast),
    LUNCH(R.string.meal_type_lunch),
    DINNER(R.string.meal_type_dinner),
    SNACK(R.string.meal_type_snack)
}