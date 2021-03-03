package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.stocks.R
import com.example.stocks.ui.StocksActivity
import com.example.stocks.ui.viewmodel.StocksViewModel
import kotlinx.android.synthetic.main.fragment_stock_information.*


class InformationStockFragment : Fragment(R.layout.fragment_stock_information) {

    lateinit var viewModel : StocksViewModel
    val args: InformationStockFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as StocksActivity).viewModel

        val stock = args.stock
        webview.apply {
            webViewClient = WebViewClient()
            loadUrl(stock.profile.weburl)
        }
    }
}