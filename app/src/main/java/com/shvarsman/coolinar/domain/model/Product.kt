package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R
import java.util.Locale

data class Product(
    val id: String = "",
    val name: String,
    val nameEn: String = name,
    val category: Category,
    val defaultUnit: MeasureUnit,
    val iconKey: String = DEFAULT_ICON_KEY,
    val isDefault: Boolean = false,
    val isToTaste: Boolean = false,
    val isAlwaysAvailable: Boolean = false
) {

    fun sortName(): String = if (Locale.getDefault().language == "en") nameEn else name

    companion object {
        const val DEFAULT_ICON_KEY = "default"
    }
}



enum class MeasureUnit(@StringRes val labelRes: Int) {
    GRAM(R.string.unit_gram),
    KILOGRAM(R.string.unit_kilogram),
    MILLILITER(R.string.unit_milliliter),
    LITER(R.string.unit_liter),
    PIECE(R.string.unit_piece),
    TABLESPOON(R.string.unit_tablespoon),
    TEASPOON(R.string.unit_teaspoon),
    PACK(R.string.unit_pack)
}
