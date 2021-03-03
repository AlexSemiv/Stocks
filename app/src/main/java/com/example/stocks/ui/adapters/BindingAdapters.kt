package com.example.stocks.ui.adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.math.RoundingMode

@BindingAdapter("app:roundValueToText")
fun roundValueToText(view: TextView, value: Double) {
    view.text = "+ ${value.toBigDecimal().setScale(3, RoundingMode.UP).toDouble()} $"
}