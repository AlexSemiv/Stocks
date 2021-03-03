package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.example.stocks.R
import com.example.stocks.util.Utils.Companion.SEARCHING_TIME_DELAY
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_stocks.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchStocksFragment(

) : StockFragment(R.id.action_searchStocksFragment_to_informationStockFragment) {
    override fun getLiveData() = viewModel.searchStocksLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showEditText()

        stocksAdapter.setOnFloatingActionButtonClickListener { stock ->
            viewModel.saveStockToSavedFragment(stock)
            Snackbar.make(view,"Stock was successfully saved", Snackbar.LENGTH_SHORT).show()
        }

        var searchJob: Job? = null
        edit_text.addTextChangedListener {
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(SEARCHING_TIME_DELAY)
                it?.let {
                    val query = it.toString()
                    if(query.isNotEmpty()){
                        viewModel.searchStocks(query)
                    }
                }
            }
        }
    }
}