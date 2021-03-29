package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.SearchView
import com.example.stocks.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class SearchStocksFragment(
) : StocksFragment(R.id.action_searchStocksFragment_to_informationStockFragment) {
    override fun setLiveData() = viewModel.searchLiveData

    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_menu_search, menu)
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem?.actionView as SearchView

        // mechanism of searching stocks by input query in searchView
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