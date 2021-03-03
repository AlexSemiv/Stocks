package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocks.R
import com.example.stocks.db.Stock
import com.example.stocks.ui.StocksActivity
import com.example.stocks.ui.viewmodel.StocksViewModel
import com.example.stocks.ui.adapters.StocksAdapter
import com.example.stocks.util.Resource
import kotlinx.android.synthetic.main.fragment_stocks.*

abstract class StockFragment(
        private val navGraphAction: Int
): Fragment(R.layout.fragment_stocks) {
    lateinit var viewModel: StocksViewModel
    lateinit var stocksAdapter: StocksAdapter

    abstract fun getLiveData(): LiveData<Resource<List<Stock>>>

    //abstract fun getNavGraphAction(): Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as StocksActivity).viewModel
        stocksAdapter = StocksAdapter()

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

        getLiveData().observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { listStocks ->
                        stocksAdapter.differ.submitList(listStocks)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
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

    fun showEditText(){
        edit_text.visibility = View.VISIBLE
    }
}
