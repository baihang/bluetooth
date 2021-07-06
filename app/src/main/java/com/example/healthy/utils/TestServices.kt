package com.example.healthy.utils

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.Image
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import com.example.healthy.R

//工作线程名称
class TestServices: IntentService("testServices") {

    companion object{
        val TAG = "TestService"
    }

    override fun onCreate() {
        Log.e(TAG, "on create")
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "on bind")
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "on start command")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    override fun onRebind(intent: Intent?) {
        Log.e(TAG, "on rebind")
        super.onRebind(intent)
    }



    override fun onHandleIntent(intent: Intent?) {
        Log.e(TAG, "on Handle Intent" + intent?.dataString)
        Log.e(TAG, Thread.currentThread().name+ " thread")
        val pending = intent?.getParcelableExtra<PendingIntent>("pendingIntent")
        pending?.send()
        val send = Intent("com.healthy.testService")
        send.putExtra("service", "TestService")
        pending?.send(this, 2, send)
        Thread.sleep(2000)

    }

    override fun onDestroy() {
        Log.e(TAG, "on destroy")
        super.onDestroy()
    }
}