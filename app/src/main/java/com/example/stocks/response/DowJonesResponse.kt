package com.example.stocks.response

data class DowJonesResponse(
    val constituents: List<String>,
    val symbol: String
)