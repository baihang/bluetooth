package com.example.healthy.data

class PulseData : BaseData(){


    init {
        headData = arrayOf(0xAA, 0xAA, 0x06, 0x0B)
        dataInit()
        label = "脉搏"
    }

    override fun getUploadLabel(): String {
        return "_mb"
    }

}