package com.example.stocks.db

import androidx.room.TypeConverter
import com.example.stocks.response.CandleResponse
import com.example.stocks.response.CompanyProfileResponse
import com.example.stocks.response.QuoteResponse
import com.example.stocks.response.news.CompanyNewsResponse
import com.example.stocks.response.news.CompanyNewsResponseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListNewsConverter {
    @TypeConverter
    fun fromNewsArrayList(list: CompanyNewsResponse): String{
        val type = object :TypeToken<CompanyNewsResponse>() {}.type
        return Gson().toJson(list,type)
    }

    @TypeConverter
    fun toNewsArrayList(string: String): CompanyNewsResponse{
        val type = object :TypeToken<CompanyNewsResponse>() {}.type
        return Gson().fromJson(string, type)
    }
}
class CandleConverter {
    @TypeConverter
    fun fromCandle(item: CandleResponse): String{
        val type = object :TypeToken<CandleResponse>() {}.type
        return Gson().toJson(item,type)
    }

    @TypeConverter
    fun toCandle(string: String): CandleResponse{
        val type = object :TypeToken<CandleResponse>() {}.type
        return Gson().fromJson(string, type)
    }
}
class QuoteConverter {
    @TypeConverter
    fun fromQuote(item: QuoteResponse): String{
        val type = object :TypeToken<QuoteResponse>() {}.type
        return Gson().toJson(item,type)
    }

    @TypeConverter
    fun toQuote(string: String): QuoteResponse{
        val type = object :TypeToken<QuoteResponse>() {}.type
        return Gson().fromJson(string, type)
    }
}