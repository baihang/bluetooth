package com.example.healthy.data

class TemperatureData : BaseData(){


    init {
        headData = arrayOf(0xAA, 0xDA, 0x04, 0x03)
        dataInit()
        label = "温度"
    }

    override fun getUploadLabel(): String {
        return "_tm"
    }

}