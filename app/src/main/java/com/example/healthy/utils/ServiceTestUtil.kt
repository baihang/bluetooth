package com.example.healthy.utils

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log

fun testBinderService(context: Context?) {
    val connect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val myBinder = service as TestBindService.MyBinder
            val s = myBinder.getService()
            Log.e("testBinderService", "service return " + s.testMethod())
            s.foreground(context)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }

    }
    Intent(context, TestBindService::class.java).also {
        context?.bindService(it, connect, Context.BIND_AUTO_CREATE)
    }
}

fun testStartService(activity: Context?) {
    Intent(activity, TestServices::class.java).also {
        it.data = Uri.parse("data")
        val broadIntent = Intent("com.healthy.testService")
        val pendingIntent =
            PendingIntent.getBroadcast(activity, 1, broadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        it.putExtra("pendingIntent", pendingIntent)
        activity?.startService(it)
    }
}

fun testMessenger(context: Context?) {
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val messenger = Messenger(service)
            val message = Message()
            message.arg1 = 1234
            messenger.send(message)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
    Intent(context, TestMessengerService::class.java).also {
        context?.bindService(it, connection, Context.BIND_AUTO_CREATE)
    }
}

class ServiceTestUtil {
}