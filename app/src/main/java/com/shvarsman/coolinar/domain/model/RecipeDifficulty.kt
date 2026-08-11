package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R

enum class RecipeDifficulty(@StringRes val labelRes: Int) {
    EASY(R.string.difficulty_easy),
    MEDIUM(R.string.difficulty_medium),
    HARD(R.string.difficulty_hard)
}