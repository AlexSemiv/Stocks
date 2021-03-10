package com.example.stocks.repository

import com.example.stocks.api.SafeApiRequest
import com.example.stocks.db.SavedStockDatabase
import com.example.stocks.db.Stock
import com.example.stocks.response.RetrofitInstance
import dagger.hilt.EntryPoint

class StocksRepository(
        val db: SavedStockDatabase
): SafeApiRequest() {
    // retrofit
    suspend fun getTopStocksTickers(symbol: String) =
            handleApiRequest {
                RetrofitInstance.api.getTopStocksTickers(symbol)
            }

    suspend fun searchStock(query: String) =
            handleApiRequest {
                RetrofitInstance.api.searchStock(query)
            }

    suspend fun getCompanyProfile2(symbol: String) =
            handleApiRequest {
                RetrofitInstance.api.getCompanyProfile2(symbol)
            }

    suspend fun getQuote(symbol: String) =
            handleApiRequest {
                RetrofitInstance.api.getQuote(symbol)
            }

    suspend fun getCompanyNews(symbol: String, from: String, to: String) =
            handleApiRequest {
                RetrofitInstance.api.getCompanyNews(symbol, from, to)
            }

    suspend fun getCandle(symbol: String, resolution: String, from: String, to: String) =
            handleApiRequest {
                RetrofitInstance.api.getCandle(symbol, resolution, from, to)
            }

    // local database
    suspend fun insertStock(stock: Stock) =
            db.getStockDao().insertStock(stock)

    suspend fun deleteStock(stock: Stock) =
            db.getStockDao().deleteStock(stock)

    suspend fun insertAllStocks(list: List<Stock>) =
        list.forEach { stock ->
            db.getStockDao().insertStock(stock)
        }

    suspend fun getTickersOfSavedStocks() =
            db.getStockDao().getTickersOfSavedStocks()

    suspend fun deleteAllSavedStocks() =
            db.getStockDao().deleteAllSavedStocks()

    suspend fun getAllSavedStocks() =
            db.getStockDao().getAllSavedStocks()
}