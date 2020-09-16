package com.example.healthy.chart

import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MyChartData: LineData() {

    override fun calcMinMax() {
        super.calcMinMax()
        //左侧Y轴范围
        mLeftAxisMax = 800000F
        mLeftAxisMin = 300000F
    }


}