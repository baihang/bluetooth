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

    //x轴宽度
    val maxXAxisLength = 800

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
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        description.isEnabled = false
        mClipValuesToContent = false
        setDrawGridBackground(true)
        setGridBackgroundColor(Color.RED)

        keepScreenOn = true
    }

    fun initDataSet(label: String, color: Int) {
        if (data == null) {
            data = MyChartData()
        }

        val dataSet = LineDataSet(ArrayList(100), label)
        dataSet.cubicIntensity = 0.2F
        dataSet.lineWidth = 1.5f
        dataSet.color = color
        dataSet.setDrawCircles(false)
        dataSet.fillColor = Color.GRAY
        dataSet.setDrawFilled(true)
        dataSet.setDrawValues(false)

        data.dataSets.add(dataSet)
    }

    override fun invalidate() {
        data.notifyDataChanged()
        notifyDataSetChanged()
        super.invalidate()
    }

    fun addEntry(value: Int){
        dataSetAddEntry(data.dataSets[0], value)
        invalidate()
    }

    private fun dataSetAddEntry(dataSet: ILineDataSet, value: Int) {
        if (dataSet.entryCount >= maxXAxisLength) {
            dataSet.removeFirst()
        }
        if (dataSet.entryCount == 0) {
            dataSet.addEntry(Entry(0f, value.toFloat()))
            return
        }
        val last = dataSet.getEntryForIndex(dataSet.entryCount - 1)
        val entry = Entry(last.x + 1, value.toFloat())
        if (entry.x < 0) {
            //避免溢出后为负数
            entry.x = 0f
        }
        dataSet.addEntry(entry)
    }
}