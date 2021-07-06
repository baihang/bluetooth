package com.example.healthy.utils

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import java.lang.ref.WeakReference

class TestMessengerService : Service() {


    private val messenger: Messenger = Messenger(MyHandler(this))

    class MyHandler(service: Service) : Handler() {
        private var context: WeakReference<Service> = WeakReference(service)

        override fun handleMessage(msg: Message) {
            Log.e("TestMessengerService", "handleMessage " + msg.arg1)
            super.handleMessage(msg)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder
    }

}