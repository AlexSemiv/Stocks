package com.example.stocks.model

import androidx.room.PrimaryKey
import java.io.Serializable

data class CompanyProfile2Response(
    val country: String,
    val currency: String,
    val exchange: String,
    val finnhubIndustry: String,
    val ipo: String,
    val logo: String,
    val marketCapitalization: Double,
    val name: String,
    val phone: String,
    val shareOutstanding: Double,
    @PrimaryKey
    val ticker: String,
    val weburl: String
): Serializable