package com.shvarsman.menuplanner.data.repository

import androidx.room.withTransaction
import com.shvarsman.menuplanner.data.local.AppDatabase
import com.shvarsman.menuplanner.domain.repository.TransactionRunner
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase
) : TransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        db.withTransaction { block() }
}