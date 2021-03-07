package com.example.stocks.ui.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.fragment_stock_information.view.*

class GraphView(
        context: Context,
        attributeSet: AttributeSet
): View(context,attributeSet) {
    private val dataSet = mutableListOf<DataPoint>()
    private var xMin = 0
    private var xMax = 0
    private var yMin = 0
    private var yMax = 0

    private val dataPointPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val dataPointFillPaint = Paint().apply {
        color = Color.WHITE
    }

    private val dataPointLinePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val axisLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
    }
    fun setDots(newDataSet: List<DataPoint>){
        xMin = newDataSet.minBy { it.x }?.x ?: 0
        xMax = newDataSet.maxBy { it.x }?.x ?: 0
        yMin = newDataSet.minBy { it.y }?.y ?: 0
        yMax = newDataSet.maxBy { it.y }?.y ?: 0

        dataSet.clear()
        dataSet.addAll(newDataSet)

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        dataSet.forEachIndexed() { index, currentDataPoint ->
            val realX = currentDataPoint.x.toRealX()
            val realY = currentDataPoint.y.toRealY()

            if(index < dataSet.size - 1){
                val nextDataPoint = dataSet[index + 1]
                val startX = currentDataPoint.x.toRealX()
                val startY = currentDataPoint.y.toRealY()
                val endX = nextDataPoint.x.toRealX()
                val endY = nextDataPoint.y.toRealY()

                canvas?.drawLine(startX, startY, endX, endY, dataPointLinePaint)
            }

            canvas?.drawCircle(realX,realY,7f,dataPointFillPaint)
            canvas?.drawCircle(realX,realY,7f,dataPointPaint)
        }

        canvas?.drawLine(0f,0f,0f,height.toFloat(),axisLinePaint)
        canvas?.drawLine(0f,height.toFloat(),width.toFloat(),height.toFloat(),axisLinePaint)
    }

    private fun Int.toRealX() = toFloat() / xMax * width
    private fun Int.toRealY() = toFloat() / yMax * height
}