package com.example.healthy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.example.healthy.db.AbstractAppDataBase
import com.example.healthy.utils.LogUtil
import com.example.healthy.utils.ThreadUtil
import com.example.healthy.utils.TokenRefreshUtil
//import com.tencent.bugly.Bugly

class MyApplication : Application() {

    companion object {
        private const val BUGLY_APP_ID = "95df789e9b"
        private const val BUGLY_APP_KEY = "47b80c77-d8f1-4122-a826-a0bc93315457"

        @SuppressLint("StaticFieldLeak")
        private lateinit var globalContext : Context
    }

    override fun onCreate() {
        super.onCreate()
        if (false) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }
        globalContext = applicationContext
        ThreadUtil.getInstance()?.timingSwitch(true)
        AbstractAppDataBase.getInstance(applicationContext)
        TokenRefreshUtil.getInstance(applicationContext)
        LogUtil.getInstance(applicationContext)
//        Bugly.init(applicationContext, BUGLY_APP_ID, true)
    }

}