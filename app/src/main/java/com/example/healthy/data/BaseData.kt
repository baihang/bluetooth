package com.example.healthy.data

import android.util.Log

abstract class BaseData {

    companion object {
        private const val TAG = "BaseData"
        const val HEAD: Short = 0xAA
        const val HEAD_LENGTH = 4

        fun getDataType(value1: Short, value2: Short): BaseData? {
            val type: Int = value1.toInt().shl(8) + value2
            Log.e(TAG, "type = $type")
            return when (type) {
                0xAA07 -> {
                    HeartOneData()
                }
                0xAA06 -> {
                    PulseData()
                }
                0xAB07 -> {
                    HeartThreeData()
                }
                else -> {
                    null
                }
            }
        }
    }

    lateinit var headData: Array<Short>
    lateinit var bodyData: Array<Short>
    lateinit var trialData: Array<Short>

    fun dataInit() {
        bodyData = Array(headData[3].toInt() - 1, init = { 0 })
        trialData = arrayOf(0)
    }

}