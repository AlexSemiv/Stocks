package com.example.stocks.model.news

import java.io.Serializable

data class CompanyNewsResponseItem(
    val category: String,
    val datetime: Int,
    val headline: String,
    val id: Int,
    val image: String,
    val related: String,
    val source: String,
    val summary: String,
    val url: String
): Serializable