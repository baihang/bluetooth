package com.example.healthy.ui.main.data

import com.example.healthy.data.BaseData
import com.example.healthy.utils.NetWortUtil

class DataRepository {
    companion object {
        fun uploadData(mobile: String, data: BaseData) {
            val param = HashMap<String, String>()
            param["phonenum"] = mobile
            param["date"] = data.timeStamp.toString()
            param["lead"] = data.label
            param["base"] = "0"
            param["data"] = data.getAllData()
            NetWortUtil.post("/msg/uploadEcg", param);
        }

        fun uploadFile() {

        }
    }

}