package com.example.stocks.repository

import com.example.stocks.api.SafeApiRequest
import com.example.stocks.api.StocksApi
import com.example.stocks.db.Stock
import com.example.stocks.db.StocksDao
import com.example.stocks.model.CandleResponse
import com.example.stocks.model.CompanyProfile2Response
import com.example.stocks.model.DowJonesResponse
import com.example.stocks.model.QuoteResponse
import com.example.stocks.model.news.CompanyNewsResponse
import com.example.stocks.model.search.SearchResponse
import javax.inject.Inject

class StocksRepository
@Inject constructor(
        private val api: StocksApi,
        private val dao: StocksDao
): SafeApiRequest() {
    suspend fun getTopStocksTickers(symbol: String): DowJonesResponse? =
            handleApiRequest {
                api.getTopStocksTickers(symbol)
            }

    suspend fun searchStock(query: String): SearchResponse? =
            handleApiRequest {
                api.searchStock(query)
            }

    suspend fun getCompanyProfile2(symbol: String): CompanyProfile2Response? =
            handleApiRequest {
                api.getCompanyProfile2(symbol)
            }

    suspend fun getQuote(symbol: String): QuoteResponse? =
            handleApiRequest {
                api.getQuote(symbol)
            }

    suspend fun getCompanyNews(symbol: String, from: String, to: String): CompanyNewsResponse? =
            handleApiRequest {
                api.getCompanyNews(symbol, from, to)
            }

    suspend fun getCandle(symbol: String, resolution: String, from: String, to: String): CandleResponse? =
            handleApiRequest {
                api.getCandle(symbol, resolution, from, to)
            }

    suspend fun insertStock(stock: Stock) = dao.insertStock(stock)

    suspend fun deleteStock(stock: Stock) = dao.deleteStock(stock)

    suspend fun insertAllStocks(list: List<Stock>) = list.forEach{ insertStock(it) }

    suspend fun getTickersOfSavedStocks(): List<String> = dao.getTickersOfSavedStocks()

    suspend fun deleteAllSavedStocks() = dao.deleteAllSavedStocks()

    suspend fun getAllSavedStocks(): List<Stock> = dao.getAllSavedStocks()
}