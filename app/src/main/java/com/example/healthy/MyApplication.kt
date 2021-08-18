package com.example.healthy

import android.app.Application
import android.util.Log
import com.example.healthy.db.AbstractAppDataBase
import com.example.healthy.utils.ThreadUtil
import java.util.concurrent.atomic.AtomicInteger

class MyApplication :Application(){

    override fun onCreate() {
        super.onCreate()
        ThreadUtil.getInstance()?.timingSwitch(true)
        AbstractAppDataBase.getInstance(applicationContext)
        Log.e("Application", "processor = " + Runtime.getRuntime().availableProcessors())
    }

}