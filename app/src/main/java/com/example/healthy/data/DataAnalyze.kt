package com.example.healthy.data

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
    }

    /**
     * 整个数据包长度
     */
    private var dataPackLength = 15
    private var headLength = 3
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

    private lateinit var headList: Array<Byte>

    private var dataModel = DATA_MODEL_NONE

    constructor() {
    }

    constructor(
        packLength: Int,
        head: Array<Byte>,
        trailLength: Int
    ) {
        this.dataPackLength = packLength
        this.headLength = head.size
        headList = head
        this.trailLength = trailLength
    }

    fun inputData(data: Array<Byte>) {
        for (byte in data) {
            when (status) {
                STATUS_NONE -> {
                    if (byte == headList[0]) {
                        status = STATUS_HEAD
                        headStatus = 0
                    }
                }
                STATUS_HEAD -> {
                    if (byte == headList[headStatus]) {
                        headStatus++
                        if (headStatus == headLength) {
                            status = STATUS_BODY
                            bodyStatus = 0
                        }
                    } else {
                        status = STATUS_NONE
                    }
                }
                STATUS_BODY -> {
                    bodyStatus++
                    if (bodyStatus == bodyLength) {
                        status = STATUS_TRAIL
                        trailStatus = 0
                    }
                }
                STATUS_TRAIL -> {
                    trailStatus++
                    if (trailStatus == trailLength) {
                        status = STATUS_NONE
                    }
                }
            }
        }
    }

}