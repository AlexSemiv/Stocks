package com.example.stocks.model.search

data class SearchResponse(
    val count: Int,
    val result: List<Result>,
    val error: String?
)