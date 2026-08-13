package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

/** Отслеживает, какие contextual tips (подсказки маскота при первом входе
 * на экран) пользователь уже видел — чтобы показать каждую не более
 * одного раза. tipId — произвольный уникальный строковый идентификатор
 * подсказки, задаётся на месте вызова (например "fridge_swipe_delete"). */
interface TipsRepository {
    fun isTipSeen(tipId: String): Flow<Boolean>
    suspend fun markTipSeen(tipId: String)
}