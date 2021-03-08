package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import com.example.stocks.R
import com.google.android.material.snackbar.Snackbar

class TopStocksFragment(
) : StockFragment(R.id.action_topStocksFragment_to_informationStockFragment) {
    override fun getLiveData() = viewModel.topStocksLiveData
}