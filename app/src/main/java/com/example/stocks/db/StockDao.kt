package com.example.stocks.db

import androidx.room.*

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock): Long

    @Delete
    suspend fun deleteStock(stock: Stock)

    @Query("SELECT ticker FROM stocks")
    suspend fun getTickersOfSavedStocks(): List<String>

    @Query("DELETE FROM stocks")
    suspend fun deleteAllStock()

    @Query("SELECT * FROM stocks")
    suspend fun getAllSavedStocks() : List<Stock>
}