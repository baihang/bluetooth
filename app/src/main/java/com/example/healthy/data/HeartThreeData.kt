package com.example.healthy.data

/**
 * 三导联心电数据
 */
open class HeartThreeData : BaseData() {


    init {
        headData = arrayOf(0xAA, 0xAB, 0x07, 0x0A)
        dataInit()
        valueArray = Array(3, init = {
            arrayOf(0)
        })
        label = "三导联心电"
    }

    override fun getUploadLabel(): String {
        return "_sl"
    }

    override fun getData(): Array<Array<Int>> {
        for (index in bodyData.indices step 3) {
            val value = byte2Int(bodyData[index], bodyData[index + 1], bodyData[index + 2])
            valueArray[index / 3 % 3][index / 3 / 3] = value
        }
        return valueArray
    }

    fun byte2Int(b1: Int, b2: Int, b3: Int): Int {
        return b1.toInt().shl(16) + b2.toInt().shl(8) + b3
    }

}