package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.SearchView
import androidx.core.widget.addTextChangedListener
import com.example.stocks.R
import com.example.stocks.ui.StocksActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_stocks.*
import kotlinx.coroutines.*

class SearchStocksFragment(
) : StockFragment(R.id.action_searchStocksFragment_to_informationStockFragment) {
    override fun setLiveData() = viewModel.searchStocksLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_menu_search, menu)
        val searchView = SearchView((context as StocksActivity).supportActionBar?.themedContext ?: context)
        val menuItem = menu.findItem(R.id.search).apply {
            actionView = searchView
        }

        var searchJob: Job? = null
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel()
                searchJob = MainScope().launch {
                        if (query != null) {
                            if (query.isNotEmpty()) {
                                viewModel.searchStocks(query)
                            } else {
                                Snackbar.make(requireView(), "Input some symbols or name of company", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
}