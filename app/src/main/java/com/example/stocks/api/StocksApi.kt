package com.example.stocks.api

import com.example.stocks.response.CandleResponse
import com.example.stocks.response.CompanyProfileResponse
import com.example.stocks.response.DowJonesResponse
import com.example.stocks.response.QuoteResponse
import com.example.stocks.response.news.CompanyNewsResponse
import com.example.stocks.response.search.SearchResponse
import com.example.stocks.util.Utils.Companion.API_TOKEN
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StocksApi {

    // get top of stocks for topStocksFragment
    @GET("index/constituents")
    suspend fun getTopStocksTickers(
            @Query("symbol")
            symbol: String,
            @Query("token")
            token: String = API_TOKEN
    ) : Response<DowJonesResponse>

    // get information about some company
    @GET("stock/profile2")
    suspend fun getCompanyProfile2(
            @Query("symbol")
            symbol: String,
            @Query("token")
            token: String = API_TOKEN
    ) : Response<CompanyProfileResponse>

    // get information about some stock's price
    @GET("quote")
    suspend fun getQuote(
            @Query("symbol")
            symbol: String,
            @Query("token")
            token: String = API_TOKEN
    ) : Response<QuoteResponse>

    // search stock
    @GET("search")
    suspend fun searchStock(
            @Query("q")
            inputQuery: String,
            @Query("token")
            token: String = API_TOKEN
    ) : Response<SearchResponse>

    // get company news by ticker
    @GET("company-news")
    suspend fun getCompanyNews(
            @Query("symbol")
            symbol: String,
            @Query("from")
            from: String,  // YYYY-MM-DD
            @Query("to")
            to: String,  // YYYY-MM-DD
            @Query("token")
            token: String = API_TOKEN
    ) : Response<CompanyNewsResponse>

    // get company candles for graphics
    @GET("stock/candle")
    suspend fun getCandle(
            @Query("symbol")
            symbol: String,
            @Query("resolution")
            resolution: String,
            @Query("from")  // 234341041 UNIX
            from: String,
            @Query("to")  // 234341041 UNIX
            to: String,
            @Query("token")
            token: String = API_TOKEN
    ) : Response<CandleResponse>
}