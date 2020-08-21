package com.example.healthy.data

import android.util.Log
import androidx.collection.ArraySet
import kotlin.experimental.and


class DataAnalyze {
    companion object {
        private const val TAG = "DataAnalyze"

        /**
         * 全局状态机
         */
        private const val STATUS_NONE = 0
        private const val STATUS_HEAD = 1
        private const val STATUS_BODY = 2
        private const val STATUS_TRAIL = 3

        /**
         * 数据格式
         */
        private const val DATA_MODEL_NONE = 0

        /**
         * 大端模式  高位在前，低位在后
         */
        private const val DATA_MODEL_BIG = 1

        /**
         * 小端模式
         */
        private const val DATA_MODEL_SMALL = 2

        /**
         * 所有数据种类
         */
        private val dataArrayList: Array<BaseData> = arrayOf(
            HeartOneData(),
            HeartThreeData()
        )
    }

    /**
     * 整个数据包长度
     */
    private var dataPackLength = 15
    private var headLength = 4
    private var bodyLength = 10
    private var trailLength = 2

    /**
     * 全局状态机
     */
    private var status = STATUS_NONE

    /**
     * 数据包包头状态
     */
    private var headStatus = STATUS_NONE
    private var bodyStatus = STATUS_NONE
    private var trailStatus = STATUS_NONE

    private var dataModel = DATA_MODEL_NONE

    private var headValue1: ArraySet<Short> = ArraySet()
    private var headValue2: ArraySet<Short> = ArraySet()

    init {
        for (data in dataArrayList) {
            headValue1.add(data.headData[1])
            headValue2.add(data.headData[2])
        }
        headLength = BaseData.HEAD_LENGTH

    }

    private var dataPackage: BaseData? = null
    private var value:Short = 0

    fun parseData(data: Array<Byte>) {
        for (b in data) {
            val byte: Short = (b and 0xFF.toByte()).toShort()
            when (status) {
                STATUS_NONE -> {
                    if (byte == BaseData.HEAD) {
                        status = STATUS_HEAD
                        headStatus = 1
                    }
                }
                STATUS_HEAD -> {
                    //处理数据包头,根据 [1][2] 确定数据类型
                    when (headStatus) {
                        1 -> {
                            if (headValue1.contains(byte)) {
                                headStatus++
                                value = byte
                            } else {
                                status = STATUS_NONE
                            }
                        }
                        2 ->{
                            if(headValue2.contains(byte)){
                                headStatus++
                                dataPackage = BaseData.getDataType(value, byte)
                                if(dataPackage == null){
                                    Log.e(TAG, "未找到指定类型  value1 = $value value2 = $byte")
                                }
                            }else{
                                status = STATUS_NONE
                            }
                        }
                        else ->{
                            if(byte == dataPackage?.headData?.get(headStatus) ?: 0){
                                headStatus++
                                if (headStatus >= headLength) {
                                    status = STATUS_BODY
                                    bodyStatus = 0
                                }
                            }else{
                                status = STATUS_NONE
                            }
                        }
                    }
                }
                STATUS_BODY -> {
                    dataPackage?.bodyData?.set(bodyStatus, byte)
                    bodyStatus++
                    if (bodyStatus >= bodyLength) {
                        status = STATUS_TRAIL
                        trailStatus = 0
                    }
                }
                STATUS_TRAIL -> {
                    dataPackage?.trialData?.set(trailStatus, byte)
                    trailStatus++
                    if (trailStatus == trailLength) {
                        status = STATUS_NONE
                    }
                }
            }
        }
    }

}