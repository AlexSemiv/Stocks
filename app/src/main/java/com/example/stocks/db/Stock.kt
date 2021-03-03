package com.example.stocks.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stocks.response.CompanyProfileResponse
import com.example.stocks.response.QuoteResponse
import java.io.Serializable
@Entity(
        tableName = "stocks",
        primaryKeys = ["ticker"]
)
data class Stock(
        @Embedded
        val profile : CompanyProfileResponse,
        @Embedded
        val price : QuoteResponse
) : Serializable