package com.shvarsman.menuplanner.domain.repository

/**
 * Абстракция над транзакцией БД для use case'ов, которые пишут в несколько
 * репозиториев одновременно (например, добавление записи в меню + генерация
 * списка покупок). Не даёт domain-слою напрямую зависеть от Room.
 */
interface TransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}