package com.example.stocks.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.*
import com.example.stocks.StockApplication
import com.example.stocks.db.Stock
import com.example.stocks.repository.StocksRepository
import com.example.stocks.response.DowJonesResponse
import com.example.stocks.response.search.SearchResponse
import com.example.stocks.util.Utils.Companion.DOW_JONES
import com.example.stocks.util.Resource
import kotlinx.coroutines.*
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
                TODO("get last request from db for TOP-10 if don't have an internet")
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
            //if(list.isNotEmpty()) {
                postValue(Resource.Success(list))
                repository.deleteAllStock()
                repository.insertAllStocks(list)
            //}
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

                    val companyProfile2 = companyProfile2Response.await().data
                    val quote = quoteResponse.await().data

                    if (companyProfile2?.ticker != "" &&
                            companyProfile2 != null && quote != null) {
                        add(Stock(companyProfile2, quote))
                    }
                }
            }.awaitAll()
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T>{
        if(response.isSuccessful){
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }

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
