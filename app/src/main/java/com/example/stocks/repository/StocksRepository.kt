package com.example.stocks.repository

import com.example.stocks.db.SavedStockDatabase
import com.example.stocks.db.Stock
import com.example.stocks.response.RetrofitInstance

class StocksRepository(
        val db: SavedStockDatabase
) {
    // retrofit
    suspend fun getTopStocksTickers(symbol: String) =
            RetrofitInstance.api.getTopStocksTickers(symbol)
    suspend fun searchStock(query: String) =
            RetrofitInstance.api.searchStock(query)

    suspend fun getCompanyProfile2(symbol: String) =
            RetrofitInstance.api.getCompanyProfile2(symbol)
    suspend fun getQuote(symbol: String) =
            RetrofitInstance.api.getQuote(symbol)

    // local database
    suspend fun insertStock(stock: Stock) =
            db.getStockDao().insertStock(stock)

    suspend fun deleteStock(stock: Stock) =
            db.getStockDao().deleteStock(stock)

    suspend fun getTickersOfSavedStocks() =
            db.getStockDao().getTickersOfSavedStocks()

    suspend fun deleteAllStock() =
            db.getStockDao().deleteAllStock()

    suspend fun insertAllStocks(list: List<Stock>) {
        list.forEach { stock ->
            db.getStockDao().insertStock(stock)
        }
    }
    suspend fun getAllSavedStocks() =
            db.getStockDao().getAllSavedStocks()
}