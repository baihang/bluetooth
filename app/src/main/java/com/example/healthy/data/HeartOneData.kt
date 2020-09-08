package com.example.healthy.data

/**
 * 单导联心电数据
 */
class HeartOneData : BaseData() {

    init {
        headData = arrayOf(0xAA, 0xAA, 0x07, 0x0B)
        dataInit()
        label = "单导联心电"
    }

}