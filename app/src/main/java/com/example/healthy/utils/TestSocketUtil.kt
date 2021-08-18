package com.example.healthy.utils

import android.nfc.Tag
import android.util.Log
import java.io.*
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

class TestSocketUtil {

    companion object{
        public const val PORT = 41258

    }

    @Volatile
    public var socketStatus = false

    private var socket: Socket? = null
    private var input: InputStream? = null
    private var out: OutputStream? = null

    private val TAG = "TestSocketUtil"

    private val readThread = Thread(Runnable {
        val bytes = ByteArray(1024)
        while (socketStatus && input != null) {
            val length = input?.read(bytes) ?: 0
            Log.e(TAG, "read ${String(bytes, 0, length)}")
        }
        Log.e(TAG, "read thread done")
    })

    private val mesQueue = LinkedBlockingQueue<String>(10)

    private val writeThread = Thread(Runnable {
        while (socketStatus && out != null) {
            val str = mesQueue.take()
            out?.write(str.toByteArray())
        }
    })

    init {
        Thread(Runnable {
            try {
                socket = Socket("127.0.0.1", PORT)
                out = socket?.getOutputStream()
                input = socket?.getInputStream()
                socketStatus = socket?.isConnected ?: false
                connectSocket()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }).start()

    }

    fun writeStr(){
        if(out != null){
            mesQueue.put("test the socket ${Math.random()}")
        }
    }

    private fun connectSocket() {
        readThread.start()
        writeThread.start()
    }

    fun closeSocket() {
        socketStatus = false
        input?.close()
        out?.close()
        socket?.close()
    }

}