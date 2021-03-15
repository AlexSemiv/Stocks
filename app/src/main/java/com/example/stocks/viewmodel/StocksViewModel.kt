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
    val topLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val savedLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()
    val searchLiveData: MutableLiveData<Resource<List<Stock>>> = MutableLiveData()

    init {
        refreshUI()
    }

    // call from init block and topStocks fragment to refreshing data
    fun refreshUI(){
        getTopStocks(DOW_JONES)
        updateSavedStocks()
    }

    // retrofit
    private fun getTopStocks(symbol: String) = viewModelScope.launch {
        topLiveData.initResponseLiveData {
            // pass a tickers list to init data for recycler in top-10Fragment
            repository.getTopStocksTickers(symbol)?.constituents!!.subList(0, 10)
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchLiveData.initResponseLiveData {
            // pass a tickers list to init data for recycler in searchFragment
            repository.searchStock(query)?.result!!.map { it.symbol }.subList(0, 10)
        }
    }

    // local database
    private fun updateSavedStocks() = viewModelScope.launch {
        savedLiveData.initSavedLiveData {
            // pass a tickers list from local database to update info in savedFragment
            repository.getTickersOfSavedStocks()
        }
    }

    // call from informationFragment to saving stock in local database
    fun saveStockToSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.insertStock(stock)
        val updateStockList = repository.getAllSavedStocks()
        savedLiveData.postValue(Resource.Success(updateStockList))
    }

    // call from savedFragment to deleting stock from local database
    fun deleteStockFromSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.deleteStock(stock)
        val updateStockList = repository.getAllSavedStocks()
        savedLiveData.postValue(Resource.Success(updateStockList))
    }

    // utils
    // init liveData object by passing a func that return list of stocks (top/search)
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initResponseLiveData(getTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stockList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                // get list of tickers
                val tickers = getTickers.invoke()
                if (tickers.isNotEmpty()) {
                    // init a data for recyclerView
                    stockList.initStockList(tickers)
                }
            } else {
                postValue(Resource.Error("No internet connection.\nPlease try again later."))
                return
            }

            // pass a data for recyclerView
            postValue(Resource.Success(stockList))
        } catch (e: Throwable) {
            when(e){
                is ApiLimitException -> postValue(Resource.Error(e.message!!))
                is IOException -> postValue(Resource.Error(e.message!!))
                else -> postValue(Resource.Error("Conversion Error.\nPlease try again later."))
            }
        }
    }

    // init liveData object by passing a func that return list of stocks (saved)
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initSavedLiveData(getSavedTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stockList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                // get list of tickers
                val tickers = getSavedTickers.invoke()
                if (tickers.isNotEmpty()) {
                    // init a data for recyclerView (updating saved stocks)
                    stockList.initStockList(tickers)
                }
            } else {
                postValue(Resource.Success(repository.getAllSavedStocks()))
                return
            }

            // pass a data for recyclerView
            postValue(Resource.Success(stockList))

            // updating data in local database
            if(stockList.isNotEmpty()) {
                repository.deleteAllSavedStocks()
                repository.insertAllStocks(stockList)
            }
        } catch (e: Throwable) {
            postValue(Resource.Success(repository.getAllSavedStocks()))
        }
    }

    // init list of stocks for recyclerView
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
