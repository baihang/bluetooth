package com.example.healthy.chart

import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MyChartData : LineData() {

    //小于0表示不再限制
    private var yMaxLimit = -1F
    private var yMinLimit = -1F

    override fun calcMinMax() {
        super.calcMinMax()
        //左侧Y轴范围
        if (yMaxLimit > 0 && mLeftAxisMax > yMaxLimit) {
            mLeftAxisMax = yMaxLimit
        }
        if (yMinLimit > 0 && mLeftAxisMin < yMinLimit) {
            mLeftAxisMin = yMinLimit
        }
    }

    fun getLeftAxisMax(): Float {
        return mLeftAxisMax
    }

    fun getLeftAxisMin(): Float {
        return mLeftAxisMin
    }

}