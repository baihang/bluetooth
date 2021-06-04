package com.example.healthy.data

class HeartSevenData : HeartThreeData() {


    init {
        label = "七导联心电"
        valueArray = Array(7, init = {
            arrayOf(0)
        })

    }

    companion object{
        val lables = arrayOf("Ⅰ", "aVR", "Ⅱ", "aVL", "Ⅲ", "aVF", "V")
    }

    override fun getData(): Array<Array<Int>> {
        super.getData()
        for (i in valueArray[0].indices) {
            valueArray[3][i] = valueArray[1][i] - valueArray[0][i]
            valueArray[4][i] = -(valueArray[1][i] + valueArray[0][i]) / 2 //aVR
            valueArray[5][i] = valueArray[0][i] - valueArray[1][i] / 2 // avL
            valueArray[6][i] = valueArray[1][i] - valueArray[0][i] / 2 //avf
        }

        val result = Array<Array<Int>>(valueArray.size, init = { Array(0, init = {0})})
        result[0] = valueArray[0]
        result[1] = valueArray[4]

        result[2] = valueArray[1]
        result[3] = valueArray[5]

        result[4] = valueArray[3]
        result[5] = valueArray[6]

        result[6] = valueArray[2]
        return result
    }


}