package com.example.stocks.ui.viewmodel

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
        topStocksLiveData.initLiveData {
            repository.getTopStocksTickers(symbol).constituents
        }
    }

    fun searchStocks(query: String) = viewModelScope.launch {
        searchStocksLiveData.initLiveData {
            repository.searchStock(query).result.map { it.symbol }
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
    private suspend fun MutableLiveData<Resource<List<Stock>>>.initLiveData(getTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        try {
            val list = mutableListOf<Stock>()

            if (hasInternetConnection()) {
                val tickers = getTickers()
                try {
                    if (tickers.isNotEmpty()) {
                        list.initStockList(tickers)
                    }
                } catch (e: Exception){
                    postValue(Resource.Error(e.message.toString()))
                }
            } else {
                postValue(Resource.Error("no internet"))
                return
            }

            postValue(Resource.Success(list))
        } catch (e: Throwable) {
            when(e){
                is IOException -> postValue(Resource.Error("Network Failure"))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun MutableLiveData<Resource<List<Stock>>>.initSavedLiveData(getSavedTickers: suspend () -> List<String>) {
        postValue(Resource.Loading())
        try {
            val list = mutableListOf<Stock>()
            if (hasInternetConnection()) {
                val tickers = getSavedTickers()
                try {
                    if (tickers.isNotEmpty()) {
                        list.initStockList(tickers)
                    }
                } catch (e: Exception){
                    postValue(Resource.Error(e.message.toString()))
                }
            } else {
                val lastSavedStocks = repository.getAllSavedStocks()
                list.addAll(lastSavedStocks)
            }

            postValue(Resource.Success(list))

            if(list.isNotEmpty()) {
                repository.deleteAllSavedStocks()
                repository.insertAllStocks(list)
            }
        } catch (e: Throwable) {
            when(e){
                is IOException -> postValue(Resource.Error("Network Failure"))
                else -> postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun MutableList<Stock>.initStockList(list: List<String>) = supervisorScope {
        val uiScope = CoroutineScope(SupervisorJob())
        list.map { item ->
                uiScope.async {
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
