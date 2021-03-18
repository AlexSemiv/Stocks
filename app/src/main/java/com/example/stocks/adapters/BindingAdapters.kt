package com.example.stocks.adapters

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.lang.StringBuilder
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("app:setDateFormat")
fun dateFormat(view: TextView, dateTime: Float) {
    try {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(dateTime.toLong() * 1000)
         view.text = sdf.format(netDate)
    } catch (e: Exception) {
        view.text = "unknown date"
    }
}

@BindingAdapter("app:setChangedPrice")
fun changedPrice(view: TextView, changedPrice: Double) {
    try {
        val str = StringBuilder()
        val rounded = changedPrice.toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
        view.text = str.apply {
            append("(")
            when {
                rounded > 0.0 -> {
                    append("+$rounded")
                    view.setTextColor(Color.GREEN)
                }
                rounded < 0.0 -> {
                    append("$rounded")
                    view.setTextColor(Color.RED)
                }
                else -> {
                    append("$rounded")
                }
            }
            append("$)")
            toString()
        }
    } catch (e: Exception){
        view.text = "unknown"
    }
}