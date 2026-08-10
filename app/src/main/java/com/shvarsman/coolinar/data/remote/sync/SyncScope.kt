package com.shvarsman.coolinar.data.remote.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Долгоживущий scope для отправки данных в Firestore в фоне. НЕ viewModelScope —
 * сохранение рецепта/продукта не должно блокироваться (и тем более виснуть)
 * в ожидании ответа от Firestore; репозитории пишут в облако "и забывают".
 */
@Singleton
class SyncScope @Inject constructor() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _pendingCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    /** Сколько push() сейчас в процессе (включая зависшие по таймауту, но ещё
     * не подтверждённые сервером) — полезно показать пользователю, что данные
     * ещё не долетели до облака, вместо тихой неопределённости. */
    val pendingCount: kotlinx.coroutines.flow.StateFlow<Int> = _pendingCount

    fun onPushStarted() {
        _pendingCount.value += 1
    }

    fun onPushFinished() {
        _pendingCount.value = (_pendingCount.value - 1).coerceAtLeast(0)
    }
}