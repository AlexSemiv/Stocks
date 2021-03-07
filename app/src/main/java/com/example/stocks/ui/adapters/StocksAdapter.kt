package com.example.stocks.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stocks.R
import com.example.stocks.databinding.ItemStockBinding
import com.example.stocks.db.Stock
import kotlinx.android.synthetic.main.item_stock.view.*

class StocksAdapter : RecyclerView.Adapter<StocksAdapter.StockViewHolder>() {
    inner class StockViewHolder(
            val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Stock>(){
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.profile.ticker == newItem.profile.ticker
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        return StockViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_stock,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = differ.currentList[position]
        holder.binding.stock = stock
        holder.itemView.apply {
            Glide.with(this)
                    .load(stock.profile.logo)
                    .override(500,500)
                    .fitCenter()
                    .error(R.drawable.ic_baseline_default_ticker_24)
                    .into(logo)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(stock)
            }
        }
    }

    private var onItemClickListener: ((Stock) -> Unit)? = null
    fun setOnItemCLickListener(listener: (Stock) -> Unit){
        onItemClickListener = listener
    }
}

