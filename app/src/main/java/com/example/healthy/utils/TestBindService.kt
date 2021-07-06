package com.example.healthy.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.os.Messenger
import android.util.Log

class TestBindService : Service() {

    companion object {
        val TAG = "TestBindService"
    }

    val binder = MyBinder()

    inner class MyBinder : Binder() {
        fun getService(): TestBindService {
            return this@TestBindService
        }
    }

    fun testMethod(): String {
        return "Binder Service"
    }

    private var fore: Boolean = false


    fun foreground(context: Context?) {
        val build = Notification.Builder(context)
            .setContentText("test bind Service")
            .setContentTitle("BindService")

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "com.test_service", "test",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(true)
            channel.lightColor = Color.RED

            val manager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            build.setChannelId("com.test_service")

        }
        val notification = build.build()
        if (fore)
            startForeground(1, notification)
        else {
            stopForeground(true)
        }
        fore = !fore
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "onBind")
        return binder
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRebind(intent: Intent?) {
        Log.e(TAG, "onRebind")
        super.onRebind(intent)
    }
}