package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import com.example.stocks.R
import com.google.android.material.snackbar.Snackbar

class TopStocksFragment(

) : StockFragment(R.id.action_topStocksFragment_to_informationStockFragment) {
    override fun getLiveData() = viewModel.topStocksLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*stocksAdapter.setOnFloatingActionButtonClickListener { stock ->
            viewModel.saveStockToSavedFragment(stock)
            Snackbar.make(view, "Stock was successfully saved", Snackbar.LENGTH_SHORT).show()
        }*/
    }

}