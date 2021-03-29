package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocks.R
import com.example.stocks.db.Stock
import com.example.stocks.ui.StocksActivity
import com.example.stocks.viewmodel.StocksViewModel
import com.example.stocks.adapters.StocksAdapter
import com.example.stocks.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_stocks.*
import javax.inject.Inject

@AndroidEntryPoint
abstract class StocksFragment(
// class that needed for us to storing same logic for each of 3 fragments in one place (
//      show recycler,
//      click on stockItem
// )
        private val navGraphAction: Int
): Fragment(R.layout.fragment_stocks) {
    lateinit var viewModel: StocksViewModel
    @Inject lateinit var stocksAdapter: StocksAdapter

    abstract fun setLiveData(): LiveData<Resource<List<Stock>>>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as StocksActivity).viewModel

        recycler.apply {
            adapter = stocksAdapter
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        stocksAdapter.setOnItemCLickListener { stock ->
            val bundle = Bundle().apply {
                putSerializable("stock", stock)
            }
            findNavController().navigate(
                    navGraphAction,
                    bundle
            )
        }

        setLiveData().observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { listStocks ->
                        stocksAdapter.differ.submitList(listStocks)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.error?.let { message ->
                        Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show()
                    }
                    response.data?.let { listStocks ->
                        stocksAdapter.differ.submitList(listStocks)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }
    private fun hideProgressBar() {
        progress_bar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progress_bar.visibility = View.VISIBLE
    }
}
