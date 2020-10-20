package com.example.healthy

import android.app.Application
import com.example.healthy.utils.ThreadUtil

class MyApplication :Application(){

    override fun onCreate() {
        super.onCreate()
        ThreadUtil.getInstance()?.timingSwitch(true)
    }

}