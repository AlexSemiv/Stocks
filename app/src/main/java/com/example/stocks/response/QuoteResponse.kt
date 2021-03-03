package com.example.stocks.response

import java.io.Serializable

data class QuoteResponse(
    val c: Double, // Current price
    val h: Double, // High price of the day
    val l: Double, // Low price of the day
    val o: Double, // Open price of the day
    val pc: Double, // Previous close price
    val t: Int
) : Serializable