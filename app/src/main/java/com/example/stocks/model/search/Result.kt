package com.example.stocks.model.search

data class Result(
    val description: String,
    val displaySymbol: String,
    val primary: List<String>?,
    val symbol: String,
    val type: String
)