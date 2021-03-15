package com.example.stocks.model

import java.io.Serializable

data class QuoteResponse(
    val c: Double, // Current price
    val h: Double, // High price of the day
    val l: Double, // Low price of the day
    val o: Double // Open price of the day
) : Serializable