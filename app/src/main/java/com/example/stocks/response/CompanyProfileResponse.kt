package com.example.stocks.response

import androidx.room.PrimaryKey
import java.io.Serializable

data class CompanyProfileResponse(
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