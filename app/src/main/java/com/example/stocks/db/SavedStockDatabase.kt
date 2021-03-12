package com.example.stocks.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
        entities = [Stock::class],
        version = 1
)
@TypeConverters(
        QuoteConverter::class,
        ListNewsConverter::class,
        CandleConverter::class
)
abstract class SavedStockDatabase: RoomDatabase() {

    abstract fun getStockDao(): StocksDao
}