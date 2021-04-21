package com.example.healthy.data

class HeartSevenData : HeartThreeData() {


    init {
        label = "七导联心电"
        valueArray = Array(7, init = {
            arrayOf(0)
        })
    }

    override fun getData(): Array<Array<Int>> {
        super.getData()
        for (i in valueArray[0].indices) {
            valueArray[3][i] = valueArray[1][i] - valueArray[0][i]
            valueArray[4][i] = -(valueArray[1][i] + valueArray[0][i]) / 2
            valueArray[5][i] = valueArray[0][i] - valueArray[1][i] / 2
            valueArray[6][i] = valueArray[1][i] - valueArray[0][i] / 2
        }
        return valueArray
    }


}