package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.stocks.R
import com.example.stocks.databinding.FragmentStockInformationBinding
import com.example.stocks.db.Stock
import com.example.stocks.ui.StocksActivity
import com.example.stocks.ui.adapters.NewsAdapter
import com.example.stocks.ui.graph.DataPoint
import com.example.stocks.ui.viewmodel.StocksViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_stock_information.*
import kotlinx.android.synthetic.main.item_stock.view.logo


class InformationStockFragment : Fragment(R.layout.fragment_stock_information) {

    lateinit var viewModel : StocksViewModel
    lateinit var newsAdapter: NewsAdapter
    private val args: InformationStockFragmentArgs by navArgs()
    private lateinit var _stock: Stock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = (activity as StocksActivity).viewModel
        newsAdapter = NewsAdapter()

        val binding: FragmentStockInformationBinding = FragmentStockInformationBinding.bind(view)
        _stock = args.stock
        binding.stock = _stock
        view.apply {
            Glide.with(this)
                    .load(_stock.profile.logo)
                    .override(200,200)
                    .fitCenter()
                    .error(R.drawable.ic_baseline_default_ticker_24)
                    .into(logo)
        }

        news_recycler.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        newsAdapter.differ.submitList(_stock.news.toList())

        newsAdapter.setOnItemCLickListener { news ->
            val bundle = Bundle().apply {
                putString("url", news.url)
            }
            findNavController().navigate(
                    R.id.action_informationStockFragment_to_newsFragment,
                    bundle
            )
        }
        graphView.setDots(
                _stock.candle.o.initGraphData(),
                _stock.candle.c.initGraphData(),
                _stock.candle.l.initGraphData(),
                _stock.candle.h.initGraphData()
        )
    }

    private fun List<Double>.initGraphData(): List<DataPoint>{
        var x =0
        return map {
            DataPoint(x++,it.toInt())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_menu_information,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.save -> {
                viewModel.saveStockToSavedFragment(_stock)
                Snackbar.make(requireView(), "Stock was successfully saved", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }
}