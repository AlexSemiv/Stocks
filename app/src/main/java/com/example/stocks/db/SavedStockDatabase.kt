package com.example.stocks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    abstract fun getStockDao(): StockDao

    companion object{

        @Volatile
        private var instance: SavedStockDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
                Room.databaseBuilder(
                        context.applicationContext,
                        SavedStockDatabase::class.java,
                        "saved_stocks_db.db"
                ).build()
    }
}