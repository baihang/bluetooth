package com.example.healthy.data

/**
 * 三导联心电数据
 */
class HeartThreeData : BaseData() {


    init {
        headData = arrayOf(0xAA, 0xAB, 0x07, 0x0D)
        dataInit()
        valueArray = Array(3, init = {
            arrayOf(0, 0)
        })
    }

    override fun getData(): Array<Array<Int>> {
        for (index in bodyData.indices step 2) {
            val value = bodyData[index].toInt().shl(8) + bodyData[index + 1]
            valueArray[index / 2 % 3][index / 2 / 3] = value
        }
        return valueArray
    }

}