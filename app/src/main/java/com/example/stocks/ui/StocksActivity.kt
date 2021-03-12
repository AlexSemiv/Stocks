package com.example.stocks.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.stocks.R
import com.example.stocks.viewmodel.StocksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_stocks.*

@AndroidEntryPoint
class StocksActivity : AppCompatActivity() {

    val viewModel by viewModels<StocksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks)

        bottomNavigationView.setupWithNavController(
            stocksNavHostFragment.findNavController()
        )
    }
}