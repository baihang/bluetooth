package com.example.healthy.chart

import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MyChartData: LineData() {

    var yMaxLimit = 5000000F
    var yMinLimit = 4000000F

    override fun calcMinMax() {
        super.calcMinMax()
        //左侧Y轴范围
        if(mLeftAxisMax > yMaxLimit){
            mLeftAxisMax = yMaxLimit
        }
        if(mLeftAxisMin < yMinLimit){
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