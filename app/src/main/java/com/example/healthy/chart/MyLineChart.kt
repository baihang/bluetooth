package com.example.healthy.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class MyLineChart : LineChart {

    companion object {
        const val MAX_X_LENGTH = 800
    }

    //x轴宽度
    var maxXAxisLength = MAX_X_LENGTH

    constructor(context: Context) : super(context)

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    constructor(context: Context, attributes: AttributeSet, defStyle: Int) : super(
        context,
        attributes,
        defStyle
    )

    override fun init() {
        super.init()
        mAxisRight.isEnabled = false
        mAxisLeft.isEnabled = false
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
//        xAxis.isEnabled = false
        description.isEnabled = false
        mClipValuesToContent = false
        mDrawBorders = true
        setDrawGridBackground(false)
        setGridBackgroundColor(Color.RED)

        keepScreenOn = true
    }

    fun setDataSetLimit(max: Int, min: Int, type: Int) {
        if (data == null) {
            throw NullPointerException("MyLineChart.dataSet == null 图像数据源为空")
        }
        (data as MyChartData).setMaxAndMin(max, min)
        (data as MyChartData).setLimitType(type)
    }

    fun initDataSet(label: String, color: Int, xMax: Int = MAX_X_LENGTH) {
        if (data == null) {
            data = MyChartData()
        }
        maxXAxisLength = xMax
        val value: ArrayList<Entry> = ArrayList(maxXAxisLength)

        for (i in 0 until maxXAxisLength) {
            value.add(Entry(i.toFloat(), 0F))
        }

        val dataSet = LineDataSet(value, label)
        dataSet.cubicIntensity = 0.2F
        dataSet.lineWidth = 1.5f
        dataSet.color = color
        dataSet.setDrawCircles(false)
        dataSet.fillColor = Color.GRAY
        dataSet.setDrawFilled(false)
        dataSet.setDrawValues(false)

        data.dataSets.add(dataSet)
    }

    override fun invalidate() {
        data?.notifyDataChanged()
        notifyDataSetChanged()
        super.invalidate()
    }

    fun addEntry(value: Int) {
        dataSetAddEntry(data.dataSets[0], value.toFloat())
        invalidate()
    }

    fun addEntry(value: Float) {
        dataSetAddEntry(data.dataSets[0], value)
        invalidate()
    }

    private fun dataSetAddEntry(dataSet: ILineDataSet, value: Float) {
        if (dataSet.entryCount >= maxXAxisLength) {
            dataSet.removeFirst()
        }
        if (dataSet.entryCount == 0) {
            dataSet.addEntry(Entry(0f, value))
            return
        }
        val last = dataSet.getEntryForIndex(dataSet.entryCount - 1)
        val entry = Entry(last.x + 1, value)
        if (entry.x < 0) {
            //避免溢出后为负数
            entry.x = 0f
        }
        dataSet.addEntry(entry)
    }
}