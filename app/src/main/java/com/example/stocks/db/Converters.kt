package com.example.stocks.db

import androidx.room.TypeConverter
import com.example.stocks.model.CandleResponse
import com.example.stocks.model.QuoteResponse
import com.example.stocks.model.news.CompanyNewsResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// converters needed to store a simple type of data in local database
// in my case I transform all data to string(JSON)

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