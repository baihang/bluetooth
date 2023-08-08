package com.example.healthy.data

class HeartSix2Data : HeartThreeData() {


    init {
        label = "六导联心电"
        headData = arrayOf(0xAA, 0xBA, 0x07, 0x03)
        dataInit()
        valueArray = Array(2, init = {
            Array(128, init = {0})
        })
        bodyData = Array(900, init = { 0 })
    }

    override fun getUploadLabel(): String {
        return "_s2l"
    }

    override fun getData(): Array<Array<Int>> {
        for (index in (0 until  (bodyData.size - 4)) step 7) {
            valueArray[0][index / 7] =
                bodyData[index + 1].toInt().shl(16) +
                    bodyData[index + 2].toInt().shl(8) +
                    bodyData[index + 3].toInt()
            valueArray[1][index / 7] =
                bodyData[index + 4].toInt().shl(16) +
                        bodyData[index + 5].toInt().shl(8) +
                        bodyData[index + 6].toInt()
        }
        return valueArray
    }

}