package com.example.healthy.data

/**
 * 三导联心电数据
 */
class HeartThreeData : BaseData() {


    init {
        headData = arrayOf(0xAA, 0xAB, 0x07, 0x0D)
        dataInit()
    }

}