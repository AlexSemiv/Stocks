package com.example.stocks.util

// generic class that help us to handle all states of data
sealed class Resource<T>(
        var data: T? = null,
        val error: String? = null
) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(error: String, data: T? = null) : Resource<T>(data,error)
    class Loading<T> : Resource<T>()
}