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
import com.example.stocks.util.ApiAccessException
import com.example.stocks.util.ApiLimitException
import com.example.stocks.util.OtherApiException
import com.example.stocks.util.Resource
import com.example.stocks.util.Utils.Companion.CANDLE_FROM
import com.example.stocks.util.Utils.Companion.CANDLE_RESOLUTION
import com.example.stocks.util.Utils.Companion.CANDLE_TO
import com.example.stocks.util.Utils.Companion.DOW_JONES
import com.example.stocks.util.Utils.Companion.NEWS_FROM
import com.example.stocks.util.Utils.Companion.NEWS_TO
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ConcurrentLinkedQueue


class StocksViewModel(
        application: Application,
        private val repository: StocksRepository
): AndroidViewModel(application) {
    val topStocksLiveData: MutableLiveData<Resource<ConcurrentLinkedQueue<Stock>>> = MutableLiveData()
    val searchStocksLiveData: MutableLiveData<Resource<ConcurrentLinkedQueue<Stock>>> = MutableLiveData()
    val savedStocksLiveData: MutableLiveData<Resource<ConcurrentLinkedQueue<Stock>>> = MutableLiveData()

    init {
        refreshUI()
    }

    fun refreshUI(){
        getTopStocks(DOW_JONES)
        updateSavedStocks()
    }

    /*TODO("добавить еще графики для остальных данных и подправить внещний вид графика и новостей")*/

    // retrofit
    private fun getTopStocks(symbol: String) = viewModelScope.launch {
        topStocksLiveData.initLiveData {
            repository.getTopStocksTickers(symbol).constituents.subList(0, 12)
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchStocksLiveData.initLiveData {
            repository.searchStock(query).result.map { it.symbol }.subList(0,12)
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
        savedStocksLiveData.postValue(Resource.Success(ConcurrentLinkedQueue(updateStockList)))
    }

    fun deleteStockFromSavedFragment(stock: Stock) = viewModelScope.launch {
        repository.deleteStock(stock)
        val updateStockList = repository.getAllSavedStocks()
        savedStocksLiveData.postValue(Resource.Success(ConcurrentLinkedQueue(updateStockList)))
    }

    // utils
    private suspend fun MutableLiveData<Resource<ConcurrentLinkedQueue<Stock>>>.initLiveData(getTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        try {
            val queue = ConcurrentLinkedQueue<Stock>()

            if (hasInternetConnection()) {
                try {
                    val tickers = getTickers()
                    if (tickers.isNotEmpty()) {
                        queue.initStockQueue(tickers)
                    }
                } catch (e: ApiLimitException) {
                    postValue(Resource.Error(e.message!!))
                    return
                } catch (e: ApiAccessException){
                    /* handle apiAccessException in future or buy access to api */
                } catch (e: OtherApiException){
                    /* handle specific exception from response */
                }

            } else {
                postValue(Resource.Error("No internet connection"))
                return
            }

            postValue(Resource.Success(queue))
        } catch (e: Throwable) {
            when(e){
                is IOException -> postValue(Resource.Error(e.message!!))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun MutableLiveData<Resource<ConcurrentLinkedQueue<Stock>>>.initSavedLiveData(getSavedTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        try {
            val queue = ConcurrentLinkedQueue<Stock>()
            if (hasInternetConnection()) {
                val tickers = getSavedTickers()
                try {
                    if (tickers.isNotEmpty()) {
                        queue.initStockQueue(tickers)
                    }
                } catch (e: ApiLimitException) {
                    postValue(Resource.Error(e.message!!))
                    return
                } catch (e: ApiAccessException){
                    /* handle apiAccessException in future or buy access to api */
                } catch (e: OtherApiException){
                    /* handle specific exception from response */
                }
            } else {
                val lastSavedStocks = repository.getAllSavedStocks()
                queue.addAll(lastSavedStocks)
            }

            postValue(Resource.Success(queue))
            if(queue.isNotEmpty()) {
                repository.deleteAllSavedStocks()
                repository.insertAllStocks(queue.toList())
            }
        } catch (e: Throwable) {
            when(e){
                is IOException -> postValue(Resource.Error(e.message!!))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun ConcurrentLinkedQueue<Stock>.initStockQueue(queue: List<String>) = supervisorScope {
        val uiScope = CoroutineScope(SupervisorJob())
        queue.map { item ->
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

                add(Stock(companyProfile2Response.await(),
                        quoteResponse.await(),
                        newsResponse.await(),
                        candleResponse.await()))
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
