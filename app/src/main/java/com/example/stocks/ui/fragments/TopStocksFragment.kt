package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.*
import com.example.stocks.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopStocksFragment(
) : StockFragment(R.id.action_topStocksFragment_to_informationStockFragment) {
    override fun setLiveData() = viewModel.topStocksLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_menu_top,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.refresh -> {
                viewModel.refreshUI()
                true
            }
            else -> false
        }
    }
}