package com.example.healthy.chart

import com.example.healthy.utils.SharedPreferenceUtil
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MyChartData() : LineData() {

    companion object {
        const val TYPE_NOT_LIMIT = 0
        const val TYPE_SCOPE_LIMIT = 1
        const val TYPE_LIMIT_FIXED = 2
    }

    //小于0表示不再限制
    private var yMaxLimit = -1F
    private var yMinLimit = -1F
    private var type = TYPE_NOT_LIMIT

    override fun calcMinMax() {
        super.calcMinMax()
        //左侧Y轴范围
        if (type == TYPE_SCOPE_LIMIT) {
            if (yMaxLimit > 0 && mLeftAxisMax > yMaxLimit) {
                mLeftAxisMax = yMaxLimit
            }
            if (yMinLimit > 0 && mLeftAxisMin < yMinLimit) {
                mLeftAxisMin = yMinLimit
            }
        }else if(type == TYPE_SCOPE_LIMIT && yMaxLimit != yMinLimit){
            mLeftAxisMax = yMaxLimit
            mLeftAxisMin = yMinLimit
        }
    }

    fun getLeftAxisMax(): Float {
        return mLeftAxisMax
    }

    fun getLeftAxisMin(): Float {
        return mLeftAxisMin
    }

    fun setMaxAndMin(max: Int, min: Int) {
        yMaxLimit = max.toFloat()
        yMinLimit = min.toFloat()
    }

    fun setLimitType(type: Int) {
        this.type = type
    }

}