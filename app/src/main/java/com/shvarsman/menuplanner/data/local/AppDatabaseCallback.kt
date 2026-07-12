package com.shvarsman.menuplanner.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shvarsman.menuplanner.data.seed.SeedProductCsvParser
import com.shvarsman.menuplanner.domain.model.Product

/**
 * При первом создании файла базы данных заполняет таблицу продуктов
 * встроенным каталогом из assets/seed_products.csv. Выполняется один раз —
 * только когда БД создаётся с нуля, не при каждом запуске приложения.
 * Эти продукты помечаются isDefault = true и защищены от удаления
 * (см. DeleteProductUseCase).
 */
class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        seedDefaultProducts(db)
    }

    private fun seedDefaultProducts(db: SupportSQLiteDatabase) {
        val csvText = context.assets.open("seed_products.csv").bufferedReader().use { it.readText() }
        val rows = SeedProductCsvParser.parse(csvText)

        db.beginTransaction()
        try {
            rows.forEach { row ->
                val values = ContentValues().apply {
                    put("name", row.name)
                    put("category", row.category.name)
                    put("defaultUnit", row.unit.name)
                    put("iconKey", Product.DEFAULT_ICON_KEY)
                    put("isDefault", 1)
                }
                db.insert("products", SQLiteDatabase.CONFLICT_IGNORE, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}