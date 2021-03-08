package com.example.stocks.api

import com.example.stocks.util.ApiException
import com.example.stocks.util.Resource
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.lang.StringBuilder

abstract class SafeApiRequest {
    suspend fun <T> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()

        if(response.isSuccessful){
            return  response.body()!!
        } else {
            val errorBody = response.errorBody()?.string()
            val message = StringBuilder()
            errorBody?.let { errorBody ->
                try {
                    message.append(JSONObject(errorBody).getString("error"))
                } catch (e: JSONException){
                    message.append("Unknown error")
                }
                message.append("\n")
            }
            message.append("Error code: ${response.code()}")

            throw ApiException(message.toString())
        }
    }
}