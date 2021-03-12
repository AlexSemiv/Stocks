package com.example.stocks.repository

import com.example.stocks.api.SafeApiRequest
import com.example.stocks.api.StocksApi
import com.example.stocks.db.Stock
import com.example.stocks.db.StocksDao
import javax.inject.Inject

class StocksRepository
@Inject constructor(
        private val api: StocksApi,
        private val dao: StocksDao
): SafeApiRequest() {
    // retrofit
    suspend fun getTopStocksTickers(symbol: String) =
            handleApiRequest {
                api.getTopStocksTickers(symbol)
            }

    suspend fun searchStock(query: String) =
            handleApiRequest {
                api.searchStock(query)
            }

    suspend fun getCompanyProfile2(symbol: String) =
            handleApiRequest {
                api.getCompanyProfile2(symbol)
            }

    suspend fun getQuote(symbol: String) =
            handleApiRequest {
                api.getQuote(symbol)
            }

    suspend fun getCompanyNews(symbol: String, from: String, to: String) =
            handleApiRequest {
                api.getCompanyNews(symbol, from, to)
            }

    suspend fun getCandle(symbol: String, resolution: String, from: String, to: String) =
            handleApiRequest {
                api.getCandle(symbol, resolution, from, to)
            }

    // local database
    suspend fun insertStock(stock: Stock) = dao.insertStock(stock)

    suspend fun deleteStock(stock: Stock) = dao.deleteStock(stock)

    suspend fun insertAllStocks(list: List<Stock>) = list.forEach { stock -> dao.insertStock(stock) }

    suspend fun getTickersOfSavedStocks() = dao.getTickersOfSavedStocks()

    suspend fun deleteAllSavedStocks() = dao.deleteAllSavedStocks()

    suspend fun getAllSavedStocks() = dao.getAllSavedStocks()
}