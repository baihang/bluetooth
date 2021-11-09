package com.example.healthy.data

class HeartSixData : HeartThreeData() {


    init {
        label = "六导联心电"
        headData = arrayOf(0xAA, 0xDA, 0x07, 0x03)
        dataInit()
        valueArray = Array(6, init = {
            Array(50, init = {0})
        })
        bodyData = Array(900, init = { 0.toShort() })
    }

    override fun getUploadLabel(): String {
        return "_ll"
    }

    override fun getData(): Array<Array<Int>> {
        for (index in bodyData.indices step 3) {
            valueArray[index / 3 % 6][index / 3 / 6] = bodyData[index].toInt().shl(16) +
                    bodyData[index + 1].toInt().shl(8) +
                    bodyData[index + 2].toInt()
        }
        return valueArray
    }

}