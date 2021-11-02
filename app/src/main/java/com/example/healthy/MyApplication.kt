package com.example.healthy

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.example.healthy.db.AbstractAppDataBase
import com.example.healthy.utils.ThreadUtil
import com.example.healthy.utils.TokenRefreshUtil

class MyApplication :Application(){

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }
        ThreadUtil.getInstance()?.timingSwitch(true)
        AbstractAppDataBase.getInstance(applicationContext)
        Log.e("Application", "processor = " + Runtime.getRuntime().availableProcessors())
        TokenRefreshUtil.getInstance(applicationContext)
    }

}