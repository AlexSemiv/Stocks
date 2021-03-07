package com.example.stocks.ui.fragments

import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.stocks.R
import com.example.stocks.databinding.FragmentStockInformationBinding
import com.example.stocks.ui.StocksActivity
import com.example.stocks.ui.adapters.NewsAdapter
import com.example.stocks.ui.graph.DataPoint
import com.example.stocks.ui.viewmodel.StocksViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_stock_information.*
import kotlinx.android.synthetic.main.fragment_stock_information.logo
import kotlinx.android.synthetic.main.fragment_stock_information.view.*
import kotlinx.android.synthetic.main.item_stock.view.*
import kotlinx.android.synthetic.main.item_stock.view.logo


class InformationStockFragment : Fragment(R.layout.fragment_stock_information) {

    lateinit var viewModel : StocksViewModel
    lateinit var newsAdapter: NewsAdapter
    private val args: InformationStockFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as StocksActivity).viewModel
        newsAdapter = NewsAdapter()

        val binding: FragmentStockInformationBinding = FragmentStockInformationBinding.bind(view)
        val stock = args.stock
        binding.stock = stock
        view.apply {
            Glide.with(this)
                    .load(stock.profile.logo)
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
        newsAdapter.differ.submitList(stock.news.toList())

        floating_button_save.setOnClickListener{
            viewModel.saveStockToSavedFragment(stock)
            Snackbar.make(view, "Stock was successfully saved", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.setOnItemCLickListener { news ->
            val bundle = Bundle().apply {
                putString("url", news.url)
            }
            findNavController().navigate(
                    R.id.action_informationStockFragment_to_newsFragment,
                    bundle
            )
        }
        //graphView.setDots(makeDataSetToGraph(stock.candle.h))
        graphView.setDots(stock.candle.o.initGraphData())
    }

    private fun List<Double>.initGraphData(): List<DataPoint>{
        var x =0
        return map {
            DataPoint(x++,it.toInt())
        }
    }

    private fun makeDataSetToGraph(list: List<Double>) : List<DataPoint>{
        val data = mutableListOf<DataPoint>()
        var x = 0
        list.forEach {
            data.add(DataPoint(x++,it.toInt()))
        }
        return data
    }
}