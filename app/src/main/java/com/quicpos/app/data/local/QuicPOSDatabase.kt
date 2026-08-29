package com.quicpos.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quicpos.app.data.local.dao.*
import com.quicpos.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ItemEntity::class,
        CategoryEntity::class,
        ReceiptEntity::class,
        ReceiptLineEntity::class,
        ModifierEntity::class,
        TaxEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class QuicPOSDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun modifierDao(): ModifierDao
    abstract fun taxDao(): TaxDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "quicpos_database"

        fun createSeedCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        // Seed default settings
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_BUSINESS_NAME}', '${SettingsEntity.DEFAULT_BUSINESS_NAME}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_POS_REGISTER_NAME}', '${SettingsEntity.DEFAULT_POS_REGISTER}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_CURRENCY_CODE}', '${SettingsEntity.DEFAULT_CURRENCY_CODE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_CURRENCY_SYMBOL}', '${SettingsEntity.DEFAULT_CURRENCY_SYMBOL}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_DARK_MODE}', '${SettingsEntity.DEFAULT_DARK_MODE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_THEME}', '${SettingsEntity.DEFAULT_THEME}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_ITEM_SIZE}', '${SettingsEntity.DEFAULT_ITEM_SIZE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_FONT_SIZE_SCALE}', '${SettingsEntity.DEFAULT_FONT_SIZE_SCALE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_BACK_OFFICE_PIN}', '${SettingsEntity.DEFAULT_BACK_OFFICE_PIN}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_LAYOUT_MODE}', '${SettingsEntity.DEFAULT_LAYOUT_MODE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_LANGUAGE}', '${SettingsEntity.DEFAULT_LANGUAGE}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_USE_BARCODE_SCANNER}', '${SettingsEntity.DEFAULT_USE_SCANNER}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_RECEIPT_HEADER}', '${SettingsEntity.DEFAULT_RECEIPT_HEADER}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_RECEIPT_FOOTER}', '${SettingsEntity.DEFAULT_RECEIPT_FOOTER}')"
                        )
                        db.execSQL(
                            "INSERT INTO settings (`key`, value) VALUES ('${SettingsEntity.KEY_RECEIPT_COUNTER}', '${SettingsEntity.DEFAULT_RECEIPT_COUNTER}')"
                        )

                        // Seed default categories
                        val now = System.currentTimeMillis()
                        db.execSQL("INSERT INTO categories (name, colorHex, sortOrder, createdAt) VALUES ('Food', '#4CAF50', 0, $now)")
                        db.execSQL("INSERT INTO categories (name, colorHex, sortOrder, createdAt) VALUES ('Drinks', '#2196F3', 1, $now)")
                        db.execSQL("INSERT INTO categories (name, colorHex, sortOrder, createdAt) VALUES ('Snacks', '#FF9800', 2, $now)")
                        db.execSQL("INSERT INTO categories (name, colorHex, sortOrder, createdAt) VALUES ('Desserts', '#E91E63', 3, $now)")

                        // Seed sample items
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Coffee', 2, 2500, 1000, 'DRK-001', 'EACH', 1, 100, '#795548', 1, 0, 1, $now, $now)
                        """)
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Iced Tea', 2, 2000, 800, 'DRK-002', 'EACH', 1, 50, '#00BCD4', 1, 0, 1, $now, $now)
                        """)
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Fried Rice', 1, 5000, 2000, 'FD-001', 'EACH', 1, 30, '#FF5722', 1, 0, 1, $now, $now)
                        """)
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Spring Roll', 3, 3000, 1200, 'SNK-001', 'EACH', 1, 40, '#FFC107', 0, 0, 1, $now, $now)
                        """)
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Mango Sticky Rice', 4, 4000, 1500, 'DST-001', 'EACH', 1, 20, '#FFEB3B', 0, 0, 1, $now, $now)
                        """)
                        db.execSQL("""
                            INSERT INTO items (name, categoryId, price, cost, sku, soldBy, trackStock, stockQuantity, colorHex, isFavorite, variablePrice, isActive, createdAt, updatedAt) 
                            VALUES ('Water Bottle', 2, 1000, 400, 'DRK-003', 'EACH', 1, 200, '#03A9F4', 1, 0, 1, $now, $now)
                        """)
                    }
                }
            }
        }
    }
}
