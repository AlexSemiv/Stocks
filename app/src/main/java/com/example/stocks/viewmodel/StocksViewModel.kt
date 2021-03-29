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
            // subList() needed to reduce the risk of throwing an exception (ApiLimitException)
            repository.getTopStocksTickers(symbol)?.constituents?.subList(0, 10) ?: listOf()
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchLiveData.initResponseLiveData {
            // pass a tickers list to init data for recycler in searchFragment
            // subList() needed to reduce the risk of throwing an exception (ApiLimitException)
            repository.searchStock(query)?.result?.map { it.symbol }?.subList(0, 10) ?: listOf()
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
        val updateStocksList = repository.getAllSavedStocks()
        savedLiveData.postValue(Resource.Success(updateStocksList))
    }

    // call from savedFragment to deleting stock from local database
    fun deleteStockFromSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.deleteStock(stock)
        val updateStocksList = repository.getAllSavedStocks()
        savedLiveData.postValue(Resource.Success(updateStocksList))
    }

    // utils
    // init liveData object by passing a func that return list of stocks (top/search)
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initResponseLiveData(getResponseTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stocksList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                // get list of tickers
                val tickers = getResponseTickers.invoke()
                if (tickers.isNotEmpty()) {
                    // init a data for recyclerView
                    stocksList.initStocksList(tickers)
                }
                // pass a data for recyclerView
                if(stocksList.isNotEmpty()) {
                    postValue(Resource.Success(stocksList))
                } else {
                    postValue(Resource.Error("No results.\nPlease try again later."))
                }
            } else {
                postValue(Resource.Error("No internet connection.\nPlease try again later."))
            }
        } catch (e: Throwable) {
            when(e){
                is ApiLimitException -> postValue(Resource.Error(e.message!!, stocksList))
                else -> postValue(Resource.Error("${e.message}.\nPlease try again later.", stocksList))
            }
        }
    }

    // init liveData object by passing a func that return list of stocks (saved)
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initSavedLiveData(getSavedTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        val stocksList = mutableListOf<Stock>()
        try {
            if (hasInternetConnection()) {
                // get list of tickers
                val tickers = getSavedTickers.invoke()
                if (tickers.isNotEmpty()) {
                    // init a data for recyclerView (updating saved stocks)
                    stocksList.initStocksList(tickers)
                }
                // pass a data for recyclerView
                postValue(Resource.Success(stocksList))
                // updating data in local database
                if(stocksList.isNotEmpty()) {
                    repository.deleteAllSavedStocks()
                    repository.insertAllStocks(stocksList)
                }
            } else {
                postValue(Resource.Success(repository.getAllSavedStocks()))
            }
        } catch (e: Throwable) {
            postValue(Resource.Success(repository.getAllSavedStocks()))
        }
    }

    // init list of stocks for recyclerView
    private suspend fun MutableList<Stock>.initStocksList(tickers: List<String>)
    {
        supervisorScope {
            val uiScope = CoroutineScope(SupervisorJob())
            tickers.map { ticker ->
                uiScope.async(Dispatchers.Main) {
                    val companyProfile2Response = async {
                        repository.getCompanyProfile2(ticker)
                    }
                    val quoteResponse = async {
                        repository.getQuote(ticker)
                    }
                    val newsResponse = async {
                        repository.getCompanyNews(ticker, NEWS_FROM, NEWS_TO)
                    }
                    val candleResponse = async {
                        repository.getCandle(ticker, CANDLE_RESOLUTION, CANDLE_FROM, CANDLE_TO)
                    }

                    val companyProfile2 = companyProfile2Response.await()
                    val quote = quoteResponse.await()
                    val news = newsResponse.await()
                    val candle = candleResponse.await()

                    if(companyProfile2 != null && quote != null && news != null && candle != null) {
                        // "ticker" may be a nullable object received from response,
                        // but in our case we can't have a nullable (val ticker: String?) "primary key" property
                        if(companyProfile2.ticker != null) {
                            if(companyProfile2.ticker.isNotEmpty()) {
                                add(Stock(companyProfile2, quote, news, candle))
                            }
                        }
                    }
                }
            }.awaitAll()
        }
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
