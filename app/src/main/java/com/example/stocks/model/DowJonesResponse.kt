package com.example.stocks.model

data class DowJonesResponse(
    val constituents: List<String>,
    val symbol: String
)