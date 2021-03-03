package com.example.stocks.response.search

data class SearchResponse(
    val count: Int,
    val result: List<Result>,
    val error: String?
)