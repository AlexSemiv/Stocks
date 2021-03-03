package com.example.stocks.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.stocks.R
import com.example.stocks.db.SavedStockDatabase
import com.example.stocks.repository.StocksRepository
import com.example.stocks.ui.viewmodel.StockViewModelProviderFactory
import com.example.stocks.ui.viewmodel.StocksViewModel
import kotlinx.android.synthetic.main.activity_stocks.*

class StocksActivity : AppCompatActivity() {

    lateinit var viewModel: StocksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks)

        val repository = StocksRepository(SavedStockDatabase(this))
        val factory = StockViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)
                .get(StocksViewModel::class.java)

        bottomNavigationView.setupWithNavController(
            stocksNavHostFragment.findNavController()
        )
    }
}