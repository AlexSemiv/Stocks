package com.example.stocks.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stocks.StockApplication
import com.example.stocks.db.Stock
import com.example.stocks.repository.StocksRepository
import com.example.stocks.util.ApiLimitException
import com.example.stocks.util.Resource
import com.example.stocks.util.Utils.Companion.CANDLE_FROM
import com.example.stocks.util.Utils.Companion.CANDLE_RESOLUTION
import com.example.stocks.util.Utils.Companion.CANDLE_TO
import com.example.stocks.util.Utils.Companion.DOW_JONES
import com.example.stocks.util.Utils.Companion.NEWS_FROM
import com.example.stocks.util.Utils.Companion.NEWS_TO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class StocksViewModel
@Inject constructor(
        application: Application,
        private val repository: StocksRepository
): AndroidViewModel(application) {
    val topStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val searchStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val savedStocksLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()

    init {
        refreshUI()
    }

    fun refreshUI(){
        getTopStocks(DOW_JONES)
        updateSavedStocks()
    }

    /*TODO( "unit-tests" +
            "java.lang.NumberFormatException: Expected an int but was 2535690129 at line 1 column 330 path $.v[0]  " +
            "websockets")*/

    // retrofit
    private fun getTopStocks(symbol: String) = viewModelScope.launch {
        topStocksLiveData.initResponseLiveData {
            repository.getTopStocksTickers(symbol)?.constituents!!.subList(0, 10)
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchStocksLiveData.initResponseLiveData {
            repository.searchStock(query)?.result!!.map { it.symbol }.subList(0, 10)
        }
    }

    // local database
    private fun updateSavedStocks() = viewModelScope.launch {
        savedStocksLiveData.initSavedLiveData {
            repository.getTickersOfSavedStocks()
        }
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
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initResponseLiveData(getTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stockList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                val tickers = getTickers.invoke()
                if (tickers.isNotEmpty()) {
                    stockList.initStockList(tickers)
                }
            } else {
                postValue(Resource.Error("No internet connection.\nPlease try again later."))
                return
            }
            postValue(Resource.Success(stockList))
        } catch (e: Throwable) {
            when(e){
                is ApiLimitException -> postValue(Resource.Error(e.message!!))
                is IOException -> postValue(Resource.Error(e.message!!))
                else -> postValue(Resource.Error("Conversion Error.\nPlease try again later."))
            }
        }
    }

    private suspend fun MutableLiveData<Resource<List<Stock>>>.initSavedLiveData(getSavedTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stockList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                val tickers = getSavedTickers.invoke()
                if (tickers.isNotEmpty()) {
                    stockList.initStockList(tickers)
                }
            } else {
                postValue(Resource.Success(repository.getAllSavedStocks()))
                return
            }
            postValue(Resource.Success(stockList))
            if(stockList.isNotEmpty()) {
                repository.deleteAllSavedStocks()
                repository.insertAllStocks(stockList)
            }
        } catch (e: Throwable) {
            postValue(Resource.Success(repository.getAllSavedStocks()))
        }
    }

    private suspend fun MutableList<Stock>.initStockList(list: List<String>) = supervisorScope {
        val uiScope = CoroutineScope(SupervisorJob())
        list.map { item ->
            uiScope.async(Dispatchers.Main) {
                val companyProfile2Response = async {
                    repository.getCompanyProfile2(item)
                }
                val quoteResponse = async {
                    repository.getQuote(item)
                }
                val newsResponse = async {
                    repository.getCompanyNews(item, NEWS_FROM, NEWS_TO)
                }
                val candleResponse = async {
                    repository.getCandle(item, CANDLE_RESOLUTION, CANDLE_FROM, CANDLE_TO)
                }

                val companyProfile2 = companyProfile2Response.await()
                val quote = quoteResponse.await()
                val news = newsResponse.await()
                val candle = candleResponse.await()

                if(companyProfile2 != null && quote != null && news != null && candle != null) {
                    if(companyProfile2.ticker != "") {
                        add(Stock(companyProfile2, quote, news, candle))
                    }
                }
            }
        }.awaitAll()
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
