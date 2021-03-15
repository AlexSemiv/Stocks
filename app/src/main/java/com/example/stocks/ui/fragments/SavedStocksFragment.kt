package com.example.stocks.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.stocks.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_stocks.*

@AndroidEntryPoint
class SavedStocksFragment(
) : StockFragment(R.id.action_savedStocksFragment_to_informationStockFragment) {
    override fun setLiveData() = viewModel.savedLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // mechanism of deleting item from recyclerView and local database by swipe
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