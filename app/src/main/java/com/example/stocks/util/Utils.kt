package com.example.stocks.util

import com.google.android.material.chip.ChipDrawable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class Utils {
    companion object{
        const val BASE_URL = "https://finnhub.io/api/v1/"
        const val API_TOKEN = "c0l6e0f48v6orbr0rekg"
        const val DOW_JONES = "^DJI"
        const val SEARCHING_TIME_DELAY = 2000L
        const val CANDLE_RESOLUTION = "M"

        val NEWS_FROM by lazy {
            getDateForCompanyNews().first
        }
        val NEWS_TO by lazy {
            getDateForCompanyNews().second
        }
        val CANDLE_FROM by lazy {
            getTimeForCandle().first
        }
        val CANDLE_TO by lazy {
            getTimeForCandle().second
        }


        private fun getDateForCompanyNews(): Pair<String,String> {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -3)

            val from = simpleDateFormat.format(calendar.time)
            val to = simpleDateFormat.format(Date())

            return Pair(from,to)
        }

        private fun getTimeForCandle(): Pair<String,String> {
            val calendar = Calendar.getInstance()

            val to = calendar.timeInMillis / 1000
            calendar.add(Calendar.MONTH, -6)
            val from = calendar.timeInMillis / 1000

            return Pair(from.toString(),to.toString())
        }
    }
}