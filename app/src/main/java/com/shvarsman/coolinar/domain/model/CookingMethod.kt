package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R

enum class CookingMethod(@StringRes val labelRes: Int) {
    BOILING(R.string.cooking_method_boiling),
    STEAMING(R.string.cooking_method_steaming),
    FRYING(R.string.cooking_method_frying),
    DEEP_FRYING(R.string.cooking_method_deep_frying),
    STEWING(R.string.cooking_method_stewing),
    OVEN_BAKING(R.string.cooking_method_oven_baking),
    BAKING_IN_POTS(R.string.cooking_method_baking_in_pots),
    GRILLING(R.string.cooking_method_grilling),
    BARBECUE(R.string.cooking_method_barbecue),
    SMOKING(R.string.cooking_method_smoking),
    SIMMERING(R.string.cooking_method_simmering),
    SOUS_VIDE(R.string.cooking_method_sous_vide),
    BLANCHING(R.string.cooking_method_blanching),
    FLAMBEING(R.string.cooking_method_flambeing),
    MICROWAVE(R.string.cooking_method_microwave),
    SLOW_COOKER(R.string.cooking_method_slow_cooker),
    AIR_FRYER(R.string.cooking_method_air_fryer),
    BREAD_MAKER(R.string.cooking_method_bread_maker),
    TOASTER_WAFFLE(R.string.cooking_method_toaster_waffle),
    NO_COOKING(R.string.cooking_method_no_cooking),
    MARINATING(R.string.cooking_method_marinating),
    SALTING_FERMENTING(R.string.cooking_method_salting_fermenting),
    CURING_DRYING(R.string.cooking_method_curing_drying),
    FREEZING(R.string.cooking_method_freezing)
}