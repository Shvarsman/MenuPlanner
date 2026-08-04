package com.shvarsman.coolinar.data.repository

import androidx.room.withTransaction
import com.shvarsman.coolinar.data.local.AppDatabase
import com.shvarsman.coolinar.domain.repository.TransactionRunner
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase
) : TransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        db.withTransaction { block() }
}