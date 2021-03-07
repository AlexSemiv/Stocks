package com.example.stocks.db

import androidx.room.Embedded
import androidx.room.Entity
import com.example.stocks.response.CandleResponse
import com.example.stocks.response.CompanyProfileResponse
import com.example.stocks.response.QuoteResponse
import com.example.stocks.response.news.CompanyNewsResponse
import java.io.Serializable
@Entity(
        tableName = "stocks",
        primaryKeys = ["ticker"]
)
data class Stock(
        @Embedded
        val profile : CompanyProfileResponse,
        val price : QuoteResponse,
        val news: CompanyNewsResponse,
        val candle: CandleResponse
) : Serializable