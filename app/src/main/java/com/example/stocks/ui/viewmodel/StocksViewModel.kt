package com.example.stocks.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stocks.StockApplication
import com.example.stocks.db.Stock
import com.example.stocks.repository.StocksRepository
import com.example.stocks.response.DowJonesResponse
import com.example.stocks.response.ErrorResponse
import com.example.stocks.response.news.CompanyNewsResponse
import com.example.stocks.response.search.SearchResponse
import com.example.stocks.util.Resource
import com.example.stocks.util.Utils.Companion.CANDLE_FROM
import com.example.stocks.util.Utils.Companion.CANDLE_RESOLUTION
import com.example.stocks.util.Utils.Companion.CANDLE_TO
import com.example.stocks.util.Utils.Companion.DOW_JONES
import com.example.stocks.util.Utils.Companion.NEWS_FROM
import com.example.stocks.util.Utils.Companion.NEWS_TO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException


class StocksViewModel(
        application: Application,
        private val repository: StocksRepository
): AndroidViewModel(application) {
    val topStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val searchStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val savedStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()

    init {
        getTopStocks(DOW_JONES)
        updateSavedStocks()
    }

    /*TODO("обработать все возможные ошибки которые может вернуть запрос" +
    "разобраться с поиском(исправить его поведение мб добавить кнопку)" +
    "добавить еще графики для остальных данных и подправить внещний вид графика и новостей")*/

    // retrofit
    private fun getTopStocks(symbol: String) = viewModelScope.launch {
        topStocksLiveData.initResponseLiveData{
            repository.getTopStocksTickers(symbol)
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchStocksLiveData.initResponseLiveData{
            repository.searchStock(query)
        }
    }

    // local database
    private fun updateSavedStocks() = viewModelScope.launch {
        savedStocksLiveData.initSavedStocksLiveData()
    }

    fun saveStockToSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.insertStock(stock)
        val updateStockList = repository.getAllSavedStocks()
        savedStocksLiveData.postValue(Resource.Success(updateStockList))
    }

    fun deleteStockFromSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.deleteStock(stock)
        val updateStockList = repository.getAllSavedStocks()
        savedStocksLiveData.postValue(Resource.Success(updateStockList))
    }

    // utils
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initResponseLiveData(
            response: suspend () -> Response<*>
    ) = with(this) {
        postValue(Resource.Loading())
        try {
            val list = mutableListOf<Stock>()

            if (hasInternetConnection()) {
                val result = when(
                        val handle = handleResponse(response()).data
                    ){
                    is DowJonesResponse -> {
                        handle.constituents.subList(0,10)
                    }
                    is SearchResponse -> {
                        handle.result.map { it.symbol }.subList(0,10)
                    }
                    else -> {
                        listOf()
                    }
                }
                try {
                    coroutineScope {
                        if(result.isNotEmpty()) {
                            list.initStockList(result)
                        }else{
                            postValue(Resource.Error("initStockLiveData error"))
                        }
                    }
                }catch (e: Exception) {
                    postValue(Resource.Error(e.message.toString()))
                }
            }else{
                postValue(Resource.Error("No internet connection"))
            }

            postValue(Resource.Success(list))
        } catch (t: Throwable) {
            when(t){
                is IOException -> postValue(Resource.Error("Network Failure"))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun MutableLiveData<Resource<List<Stock>>>.initSavedStocksLiveData(
    ) = with(this){
        postValue(Resource.Loading())
        try {
            val list = mutableListOf<Stock>()

            if (hasInternetConnection()) {
                val savedTickers = repository.getTickersOfSavedStocks()
                try {
                    if(savedTickers.isNotEmpty()) {
                        list.initStockList(savedTickers)
                    }
                }catch (e: Exception) {
                    postValue(Resource.Error(e.message.toString()))
                }
            }else{
                val lastSavedStocks = repository.getAllSavedStocks()
                list.addAll(lastSavedStocks)
            }

            postValue(Resource.Success(list))

            if(list.isNotEmpty()) {
                repository.deleteAllStock()
                repository.insertAllStocks(list)
            }
        } catch (t: Throwable) {
            when(t){
                is IOException -> postValue(Resource.Error("Network Failure"))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun MutableList<Stock>.initStockList(
            list: List<String>
    ) = with(this) {
        coroutineScope {
            list.map { item ->
                async(Dispatchers.IO) {
                    val companyProfile2Response = async {
                        val resultProfile2Response = repository.getCompanyProfile2(item)
                        handleResponse(resultProfile2Response)
                    }
                    val quoteResponse = async {
                        val resultQuoteResponse = repository.getQuote(item)
                        handleResponse(resultQuoteResponse)
                    }
                    val newsResponse = async {
                        val resultNews = repository.getCompanyNews(item, NEWS_FROM, NEWS_TO)
                        handleResponse(resultNews)
                    }
                    val candleResponse = async {
                        val resultCandle = repository.getCandle(item, CANDLE_RESOLUTION, CANDLE_FROM, CANDLE_TO)
                        handleResponse(resultCandle)
                    }

                    val companyProfile2 = companyProfile2Response.await().data
                    val quote = quoteResponse.await().data
                    val news = newsResponse.await().data
                    val candle = candleResponse.await().data

                    if (companyProfile2 != null && quote != null && news != null && candle != null) {
                        add(Stock(companyProfile2, quote, news, candle))
                    }
                }
            }.awaitAll()
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////
    private fun <T> handleResponse(response: Response<T>): Resource<T>{
        if(response.isSuccessful){
            response.body()?.let { result ->
                return when(response.code()){
                    200 -> Resource.Success(result)
                    else ->  Resource.Error("unknown code (successful)")
                }
            }
        }else{
            return when(response.code()) {
                //403 -> Resource.Error(handleErrorResponseToGson(response.errorBody()))
                429 -> Resource.Error(handleErrorResponseToGson(response.errorBody()))
                else -> Resource.Error("unknown code (unsuccessful)")
            }
        }
        return Resource.Error("handle response error")
    }

    private fun handleErrorResponseToGson(errorBody: ResponseBody?): String {
        val type = object :TypeToken<ResponseBody>() {}.type
        return Gson().toJson(errorBody,type)
    }
/////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("DEPRECATION")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<StockApplication>()
                .getSystemService(
                        Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager
                    .getNetworkCapabilities(activeNetwork) ?: return false

            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run{
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TRANSPORT_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
}
