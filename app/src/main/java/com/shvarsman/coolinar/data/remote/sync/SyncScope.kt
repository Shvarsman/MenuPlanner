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
}