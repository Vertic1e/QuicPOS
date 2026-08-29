package com.quicpos.app.di

import android.content.Context
import androidx.room.Room
import com.quicpos.app.data.local.QuicPOSDatabase
import com.quicpos.app.data.local.dao.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuicPOSDatabase {
        return Room.databaseBuilder(
            context,
            QuicPOSDatabase::class.java,
            QuicPOSDatabase.DATABASE_NAME
        )
            .addCallback(QuicPOSDatabase.createSeedCallback())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideItemDao(database: QuicPOSDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCategoryDao(database: QuicPOSDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideReceiptDao(database: QuicPOSDatabase): ReceiptDao = database.receiptDao()

    @Provides
    fun provideModifierDao(database: QuicPOSDatabase): ModifierDao = database.modifierDao()

    @Provides
    fun provideTaxDao(database: QuicPOSDatabase): TaxDao = database.taxDao()

    @Provides
    fun provideSettingsDao(database: QuicPOSDatabase): SettingsDao = database.settingsDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
