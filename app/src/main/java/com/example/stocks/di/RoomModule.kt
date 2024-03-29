package com.example.stocks.di

import android.content.Context
import androidx.room.Room
import com.example.stocks.db.SavedStocksDatabase
import com.example.stocks.db.StocksDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {

    @Provides
    @Singleton
    fun provideStockDatabase(@ApplicationContext context: Context): SavedStocksDatabase = Room.databaseBuilder(
            context.applicationContext,
            SavedStocksDatabase::class.java,
            "saved_stocks_db.db"
    ).build()

    @Provides
    @Singleton
    fun provideStockDao(savedStocksDatabase: SavedStocksDatabase): StocksDao = savedStocksDatabase.getStockDao()
}