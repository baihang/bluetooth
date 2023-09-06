package com.example.healthy.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.healthy.data.*
import okhttp3.internal.wait
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object VirtualBluetooth : AbstractBluetooth() {

    private val TAG = "VirtualBluetooth"

    private var handler: Handler? = null

    private const val MSG_SCAN_DEVICE = 1
    private const val MSG_STOP_SCAN_DEVICE = 2
    private const val MSG_CONNECT_DEVICE = 3
    private const val MSG_DESTROY = 4
    private const val MSG_RECEIVE_DATA = 5

    private val dataThread = object : Thread("virtual-bluetooth") {
        override fun run() {
            Looper.prepare()

            handler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MSG_DESTROY -> {
                            Log.e(TAG, "handle msg destroy")
                            looper.quitSafely()
                            listener?.onDeviceStatusChange(STATUS_DESTROY)
                        }
                        MSG_SCAN_DEVICE -> {
                            Log.e(TAG, "handle msg scan device")

                            listener?.onDeviceFound(null)
                        }
                        MSG_CONNECT_DEVICE -> {
                            Log.e(TAG, "handle msg connect device")

                            listener?.onDeviceStatusChange(STATUS_CONNECTED_DEVICE)
                            receiveData()
                            listener?.onDeviceStatusChange(STATUS_CONNECTED_SUCCESS)
                        }
                        MSG_RECEIVE_DATA -> {
                            val data = getVirtualData(HeartSix2Data())
                            listener?.onDataReceive(data, data.size)
                            receiveData()
                        }

                    }
//                    Log.e(TAG, "handle message ${msg.what}")
                }
            }
            Looper.loop()
        }
    }

    private fun getVirtualData(data: BaseData): ByteArray {
        Log.i(TAG, "getVirtualData " + data.label)
        val result = ByteArray(data.headData.size + data.bodyData.size + data.trialData.size)
        for (i in result.indices) {
            if (i < data.headData.size) {
                result[i] = data.headData[i].toByte()
            } else {
                result[i] = (Math.random() * 10).toInt().toByte()
            }
        }
        return result
    }

    override fun deviceInit(application: Application) {
        if (!dataThread.isAlive) {
            dataThread.start()
        }
    }

    override fun stopScanDevice() {
        val msg = handler?.obtainMessage() ?: Message()
        msg.what = MSG_STOP_SCAN_DEVICE
        handler?.sendMessageDelayed(msg, 1000)
    }

    override fun destroy(activity: Activity?) {
        val msg = handler?.obtainMessage() ?: Message()
        msg.what = MSG_DESTROY
        handler?.sendMessageDelayed(msg, 1000)
    }

    override fun scanDevice(activity: Activity?) {
        val msg = handler?.obtainMessage() ?: Message()
        msg.what = MSG_SCAN_DEVICE
        handler?.sendMessageDelayed(msg, 1000)
    }

    override fun connectDevice(context: Context?, device: BluetoothDevice?) {
        val msg = handler?.obtainMessage() ?: Message()
        msg.what = MSG_CONNECT_DEVICE
        handler?.sendMessageDelayed(msg, 1000)
    }

    fun receiveData(){
        val msg = handler?.obtainMessage() ?: Message()
        msg.what = MSG_RECEIVE_DATA
        handler?.sendMessageDelayed(msg, 50)
    }

}