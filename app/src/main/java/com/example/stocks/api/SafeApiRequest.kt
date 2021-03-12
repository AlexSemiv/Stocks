package com.example.stocks.api

import com.example.stocks.util.ApiLimitException
import com.example.stocks.util.Utils.Companion.SUCCESS_200
import com.example.stocks.util.Utils.Companion.UNSUCCESS_429
import retrofit2.Response

abstract class SafeApiRequest {
    suspend fun <T> handleApiRequest(call: suspend () -> Response<T>): T? {
        val response = call.invoke()

        return if(response.isSuccessful){
            when(response.code()){
                SUCCESS_200 -> response.body()!!
                else -> null
            }
        } else {
            when(response.code()){
                UNSUCCESS_429 -> throw ApiLimitException("API limit reached.\nPlease try again later.")
                else -> null
            }
        }
    }
}