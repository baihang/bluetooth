package com.example.healthy.data

import android.util.Log
import java.lang.StringBuilder
import java.security.Timestamp

abstract class BaseData : Cloneable{

    companion object {
        private const val TAG = "BaseData"
        const val HEAD: Short = 0xAA
        const val HEAD_LENGTH = 4

        fun getDataType(value1: Short, value2: Short): BaseData? {
            val type: Int = value1.toInt().shl(8) + value2
//            Log.e(TAG, "type = $type")
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
    lateinit var valueArray: Array<Array<Int>>
    lateinit var label:String
    var timeStamp: Long = 0L

    fun dataInit() {
        bodyData = Array(headData[3].toInt() - 1, init = { 0.toShort() })
        trialData = arrayOf(0)
        valueArray = arrayOf(Array(bodyData.size / 2, init = { 0 }))
        timeStamp = System.currentTimeMillis()
    }

    open fun getData(): Array<Array<Int>> {
        for (index in bodyData.indices step 2) {
            valueArray[0][index / 2] = bodyData[index].toInt().shl(8) + bodyData[index + 1]
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
    fun getAllData(): String{
        val builder = StringBuilder()
        for (v in headData){
            builder.append(v)
        }
        for(v in bodyData){
            builder.append(v)
        }
        for (v in trialData){
            builder.append(v)
        }
        return builder.toString()
    }

}