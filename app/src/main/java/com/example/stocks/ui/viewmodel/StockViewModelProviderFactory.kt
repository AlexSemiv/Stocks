package com.example.stocks.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stocks.repository.StocksRepository

class StockViewModelProviderFactory(
        val application: Application,
        val stocksRepository: StocksRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StocksViewModel(application, stocksRepository) as T
    }
}