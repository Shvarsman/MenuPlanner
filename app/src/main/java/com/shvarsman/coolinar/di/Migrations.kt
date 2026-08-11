package com.shvarsman.coolinar.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // По умолчанию nameEn = '' — сразу после миграции фолбэк в
        // Product.localizedName() покажет русское name, пока не пересидируется/
        // не обновится запись. Для новых установок nameEn корректно заполнится
        // сразу через AppDatabaseCallback.
        db.execSQL("ALTER TABLE products ADD COLUMN nameEn TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE products SET nameEn = name WHERE nameEn = ''")
    }
}