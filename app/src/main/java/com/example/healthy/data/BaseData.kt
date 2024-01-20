package com.example.healthy.data

import android.util.Log
import kotlin.text.StringBuilder

abstract class BaseData : Cloneable {

    companion object {
        private const val TAG = "BaseData"
        const val HEAD: Int = 0xAA
        const val HEAD_LENGTH = 4

        fun getDataType(value1: Int, value2: Int): BaseData? {
            return when (value1.shl(8) + value2) {
                0xAA07 -> {
                    HeartOneData()
                }

                0xAA06 -> {
                    PulseData()
                }

                0xAB07 -> {
                    HeartThreeData()
                }

                0xDA07 -> {
                    HeartSixData()
                }

                0xBA07 -> HeartSix2Data()
                0xDA04 -> {
                    TemperatureData()
                }

                else -> {
                    Log.e(TAG, "receive unable type data")
                    null
                }
            }
        }
    }

    lateinit var headData: Array<Int>
    lateinit var bodyData: Array<Int>
    lateinit var trialData: Array<Int>
    lateinit var valueArray: Array<Array<Int>>
    lateinit var label: String
    var timeStamp: Long = 0L

    fun dataInit() {
        bodyData = Array(headData[3] - 1, init = { 0 })
        trialData = arrayOf(0)
        valueArray = arrayOf(Array(bodyData.size / 2, init = { 0 }))
        timeStamp = System.currentTimeMillis()
    }

    open fun getData(): Array<Array<Int>> {
        for (index in bodyData.indices step 2) {
            valueArray[0][index / 2] = bodyData[index].shl(8) + bodyData[index + 1]
        }
        return valueArray
    }

    public override fun clone(): BaseData {
        val it = super.clone() as BaseData
        it.headData = this.headData.clone()
        it.bodyData = this.bodyData.clone()
        it.trialData = this.bodyData.clone()
        return it
    }

    /**
     * 获取原始数据
     */
    fun getAllData(): String {
        val builder = StringBuilder()
        for (v in headData) {
            builder.append(v)
        }
        for (v in bodyData) {
            builder.append(v)
        }
        for (v in trialData) {
            builder.append(v)
        }
        return builder.toString()
    }

    open fun getBodyData(): String {
        val builder = StringBuilder()
        val body = getData()
        for (d1 in body) {
            for (d2 in d1) {
                builder.append(d2).append(" ")
            }
        }
//        builder.append("\n")
        return builder.toString()
    }

    open fun getDataString(sb: StringBuilder? = null): String {
        val stringBuilder = sb ?: StringBuilder()
        val data = getData()
        for (item in data) {
            for (i in item) {
                stringBuilder.append(i).append(" ")
            }
        }
        return stringBuilder.toString()
    }

    abstract fun getUploadLabel(): String

}