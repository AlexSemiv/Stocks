package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.stocks.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_stocks.*


class SavedStocksFragment(
) : StockFragment(R.id.action_savedStocksFragment_to_informationStockFragment) {
    override fun getLiveData() = viewModel.savedStocksLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = true
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val stock = stocksAdapter.differ.currentList[viewHolder.adapterPosition]
                viewModel.deleteStockFromSavedFragment(stock)
                Snackbar.make(view,"Stock was successfully deleted",Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.saveStockToSavedFragment(stock)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(recycler)
        }
    }
}