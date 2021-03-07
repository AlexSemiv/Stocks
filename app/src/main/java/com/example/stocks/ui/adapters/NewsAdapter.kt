package com.example.stocks.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stocks.R
import com.example.stocks.databinding.ItemNewsBinding
import com.example.stocks.db.Stock
import com.example.stocks.response.news.CompanyNewsResponseItem
import kotlinx.android.synthetic.main.item_news.view.*
import kotlinx.android.synthetic.main.item_stock.view.*

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    inner class NewsViewHolder(
        val binding : ItemNewsBinding
    ): RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<CompanyNewsResponseItem>(){
        override fun areItemsTheSame(oldItem: CompanyNewsResponseItem, newItem: CompanyNewsResponseItem) =
            oldItem.url == newItem.url
        override fun areContentsTheSame(oldItem: CompanyNewsResponseItem, newItem: CompanyNewsResponseItem) =
            oldItem == newItem
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_news,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = differ.currentList[position]
        holder.binding.news = news
        holder.itemView.apply {
            Glide.with(this)
                    .load(news.url)
                    .override(500,500)
                    .fitCenter()
                    .error(R.drawable.ic_baseline_default_ticker_24)
                    .into(image_news)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(news)
            }
        }
    }

    private var onItemClickListener: ((CompanyNewsResponseItem) -> Unit)? = null
    fun setOnItemCLickListener(listener: (CompanyNewsResponseItem) -> Unit){
        onItemClickListener = listener
    }
}