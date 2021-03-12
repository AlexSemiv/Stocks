package com.example.stocks.ui.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GraphView(
        context: Context,
        attributeSet: AttributeSet
): View(context,attributeSet) {
    private val dataSetOpen = mutableListOf<DataPoint>()
    private val dataSetClose = mutableListOf<DataPoint>()
    private val dataSetHigh = mutableListOf<DataPoint>()
    private val dataSetLow = mutableListOf<DataPoint>()

    private var xMin = 0
    private var xMax = 0
    private var yMin = 0
    private var yMax = 0

    private val dataOpenPointLinePaint = initPoint(Color.YELLOW)
    private val dataClosePointLinePaint = initPoint(Color.BLUE)
    private val dataHighPointLinePaint = initPoint(Color.GREEN)
    private val dataLowPointLinePaint = initPoint(Color.RED)
    private fun initPoint(c: Int) = Paint().apply {
        color = c
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val dataPointPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
    private val dataPointFillPaint = Paint().apply {
        color = Color.CYAN
    }
    private val axisLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
    }

    fun setDots(dataOpen:List<DataPoint>, dataClose:List<DataPoint>, dataLow:List<DataPoint>, dataHigh:List<DataPoint>) {
        dataSetOpen.clear()
        dataOpen.initMaxMinDots()
        dataSetOpen.addAll(dataOpen)

        dataSetClose.clear()
        dataClose.initMaxMinDots()
        dataSetClose.addAll(dataClose)

        dataSetLow.clear()
        dataLow.initMaxMinDots()
        dataSetLow.addAll(dataLow)

        dataSetHigh.clear()
        dataHigh.initMaxMinDots()
        dataSetHigh.addAll(dataHigh)

        invalidate()
    }
    private fun List<DataPoint>.initMaxMinDots(){
        xMin = this.minBy { it.x }?.x ?: 0
        xMax = this.maxBy { it.x }?.x ?: 0
        yMin = 0
        yMax = getYMax(this.maxBy { it.y }?.y ?: 0)
    }
    private fun getYMax(number: Int): Int{
        var range = 50
        while(number > range){
            range += 10
        }
        return range
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(dataSetOpen,dataOpenPointLinePaint)
        canvas?.drawLine(dataSetClose, dataClosePointLinePaint)
        canvas?.drawLine(dataSetHigh,dataHighPointLinePaint)
        canvas?.drawLine(dataSetLow,dataLowPointLinePaint)
    }

    private fun Canvas.drawLine(list: MutableList<DataPoint>, line: Paint){
        list.forEachIndexed() { index, currentDataPoint ->
            val realX = currentDataPoint.x.toRealX()
            val realY = currentDataPoint.y.toRealY()
            if(index < list.size - 1){
                val nextDataPoint = list[index + 1]
                val startX = currentDataPoint.x.toRealX()
                val startY = currentDataPoint.y.toRealY()
                val endX = nextDataPoint.x.toRealX()
                val endY = nextDataPoint.y.toRealY()
                drawLine(startX, startY, endX, endY, line)
            }
            drawCircle(realX,realY,7f,dataPointFillPaint)
            drawCircle(realX,realY,7f,dataPointPaint)
        }
        drawLine(0f,0f,0f,height.toFloat(),axisLinePaint)
        drawLine(0f,height.toFloat(),width.toFloat(),height.toFloat(),axisLinePaint)
    }
    private fun Int.toRealX() = toFloat() / xMax * width
    private fun Int.toRealY() = toFloat() / yMax * height
}