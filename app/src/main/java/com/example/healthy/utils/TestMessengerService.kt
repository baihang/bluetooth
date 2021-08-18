package com.example.healthy.utils

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import java.io.*
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TestMessengerService : Service() {

    private val TAG = "TestMessengerService"

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

    /////socket 通信/////
    private var count = 0;
    private val threadPool = ThreadPoolExecutor(
        3,
        6,
        60,
        TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>()
    ) { r -> Thread(r, "socket thread pool - ${count++}") }

    override fun onCreate() {
        super.onCreate()
        openSocket()
    }

    @Volatile
    private var socketStatus = false

    private fun openSocket() {
        socketStatus = true
        threadPool.execute(Runnable {
            val socket = ServerSocket(TestSocketUtil.PORT)
            while (socketStatus) {
                Log.e(TAG, "client listener")
                val client = socket.accept()
                writeToSocket(client)
            }
        })

    }

    private fun writeToSocket(socket: Socket) {
        val run = Runnable {
            val bufferIn = socket.getInputStream()
            val write = socket.getOutputStream()
            val bytes = ByteArray(1024)
            while (socket.isConnected) {
                val len = bufferIn.read(bytes)
                Log.e(TAG, "buffer read ${String(bytes, 0, len)}")
                Thread.sleep(1000)
                write.write("hello, service received $len".toByteArray())
            }
            bufferIn.close()
            write.close()
            Log.e(TAG, "socket close")
        }
        threadPool.execute(run)
    }

    override fun onDestroy() {
        Log.e(TAG, "destroy")
        socketStatus = false
        super.onDestroy()
    }

}