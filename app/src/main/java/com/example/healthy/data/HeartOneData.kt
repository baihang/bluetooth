package com.example.healthy.data

import java.lang.StringBuilder

/**
 * 单导联心电数据
 */
class HeartOneData : BaseData() {

    init {
        headData = arrayOf(0xAA, 0xAA, 0x07, 0x0B)
        dataInit()
        label = "单导联心电"
    }

    override fun getUploadLabel(): String {
        return "_dl"
    }

    override fun getBodyData(): String{
        val builder = StringBuilder()
        val body = getData()
        for (d1 in body){
            for(d2 in d1){
                builder.append(d2).append("\n")
            }
        }
        return builder.toString()
    }

}