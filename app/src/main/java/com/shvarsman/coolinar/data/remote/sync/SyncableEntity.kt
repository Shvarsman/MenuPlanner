package com.shvarsman.coolinar.data.remote.sync

/** Реализуют все Room-сущности, которые синхронизируются с Firestore. */
interface SyncableEntity {
    val id: String
    val updatedAt: Long
    val isDeleted: Boolean
}