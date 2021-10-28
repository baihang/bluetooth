package com.example.healthy.utils

import android.util.Log
import okhttp3.Call
import okhttp3.EventListener
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 监控网络请求
 */
class NetWorkEventListener(callId: Long, startNano: Long, val call: Call) : EventListener() {

    companion object {
        private const val TAG = "NetWorkEventListener"

        val FACTORY: Factory = object : Factory {

            val nextCallId: AtomicLong = AtomicLong(1L)

            override fun create(call: Call): EventListener {
                val callId = nextCallId.getAndIncrement()
                return NetWorkEventListener(callId, System.nanoTime(), call)
            }

        }
    }

    private var callId = 0L
    private var startTimeNano = 0L

    init {
        this.callId = callId
        this.startTimeNano = startNano
    }

    private fun printEvent(name: String) {
        val time: Double = (System.nanoTime() - startTimeNano) / 1000000000.toDouble()
        Log.w(
            TAG,
            "call id = $callId event = $name at $time call usl = ${call.request().url} body = ${call.request().body}"
        )
    }

    override fun callStart(call: Call) {
        printEvent("callStart")
        Log.d(TAG, "url = ${call.request().url}")
    }

    override fun dnsStart(call: Call, domainName: String) {
        printEvent("dnsStart")
    }

    override fun callEnd(call: Call) {
        printEvent("callEnd")
    }

    override fun callFailed(call: Call, ioe: IOException) {
        printEvent("callFailed")
    }

}