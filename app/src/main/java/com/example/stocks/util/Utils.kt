package com.example.stocks.util

import com.example.stocks.ui.graph.DataPoint
import com.google.android.material.chip.ChipDrawable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

// constants, simple functions
class Utils {
    companion object{
        const val BASE_URL = "https://finnhub.io/api/v1/"
        // must be store in local properties
        const val API_TOKEN = "c0l6e0f48v6orbr0rekg"
        const val DOW_JONES = "^DJI"
        const val CANDLE_RESOLUTION = "M"
        const val SUCCESS_200 = 200
        const val UNSUCCESS_429 = 429

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
            calendar.add(Calendar.WEEK_OF_YEAR, -1)

            val from = simpleDateFormat.format(calendar.time)
            val to = simpleDateFormat.format(Date())

            return Pair(from,to)
        }

        private fun getTimeForCandle(): Pair<String,String> {
            val calendar = Calendar.getInstance()

            val to = calendar.timeInMillis / 1000
            calendar.add(Calendar.YEAR, -1)
            val from = calendar.timeInMillis / 1000

            return Pair(from.toString(),to.toString())
        }

        fun List<Double>.initGraphData(): List<DataPoint>{
            var x =0
            return map {
                DataPoint(x++,it.toInt())
            }
        }
    }
}