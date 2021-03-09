package com.example.stocks.api

import com.example.stocks.util.ApiAccessException
import com.example.stocks.util.ApiLimitException
import com.example.stocks.util.OtherApiException
import com.example.stocks.util.Utils.Companion.SUCCESS_200
import com.example.stocks.util.Utils.Companion.UNSUCCESS_403
import com.example.stocks.util.Utils.Companion.UNSUCCESS_429
import retrofit2.Response

abstract class SafeApiRequest {
    suspend fun <T> handleApiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()

        return if(response.isSuccessful){
            when(response.code()){
                SUCCESS_200 -> response.body()!!
                else -> throw OtherApiException("API received unknown SUCCESSFUL response code")
            }
        } else {
            when(response.code()){
                UNSUCCESS_429 -> throw ApiLimitException("API limit reached. Please try again later. Remaining Limit: 0")
                UNSUCCESS_403 -> throw ApiAccessException("Api access exception.")
                else -> throw OtherApiException("API received unknown UNSUCCESSFUL response code")
            }
        }
    }
}