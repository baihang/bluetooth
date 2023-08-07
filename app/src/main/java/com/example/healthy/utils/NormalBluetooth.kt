package com.example.healthy.utils

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object NormalBluetooth : AbstractBluetooth() {

    private val TAG = "NormalBluetooth"

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    @Volatile
    private var isRunning = true

    private var socket: BluetoothSocket? = null
    var secure = true

    private val dataThread = object : Thread("normal-bluetooth") {
        override fun run() {
            try {
                lock.lock()
                while (isRunning) {
                    Log.e(TAG, "thread socket connect")
                    try {
                        socket?.connect()
                        Log.e(TAG, "socket open")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        stringBuilder.append(e)
                        listener?.onDeviceStatusChange(STATUS_ERROR)
                    }
                    if (socket != null && socket?.isConnected == true) {
                        listener?.onDeviceStatusChange(STATUS_CONNECTED_SUCCESS)
                    }
                    val inStream = socket?.inputStream
                    val buffer = ByteArray(1024)
                    while (socket != null && socket?.isConnected == true && inStream != null) {
                        val len = inStream.read(buffer)
                        listener?.onDataReceive(buffer, len)
                    }
                    condition.await()
                }
                lock.unlock()
            } catch (e: Exception) {
                e.printStackTrace()
                lock.unlock()
                socket?.close()
                socket = null
                bluetoothAdapter = null
                listener?.onDeviceStatusChange(STATUS_ERROR)
            }

        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    listener?.onDeviceFound(device)
                    Log.e(TAG, "device found ${device?.address}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {

                }
            }
        }
    }

    override fun deviceInit(application: Application) {
        isRunning = true
        if (!dataThread.isAlive) {
            dataThread.start()
        }
        Log.e(TAG, "normal init")
    }

    override fun stopScanDevice() {
        bluetoothAdapter?.cancelDiscovery()
        Log.e(TAG, "stop scan normal device")
    }

    override fun destroy(activity: Activity?) {
        stopScanDevice()
        isRunning = false
        kotlin.runCatching {
            activity?.unregisterReceiver(receiver)
        }
    }

    override fun scanDevice(activity: Activity?) {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(receiver, intentFilter)
        if (bluetoothAdapter == null) {
            if (activity != null)
                baseInit(activity.application)
        }
        bluetoothAdapter?.startDiscovery()
    }

    private val stringBuilder = StringBuilder()

    override fun connectDevice(context: Context?, device: BluetoothDevice?) {
        bluetoothAdapter?.cancelDiscovery()
        if(device == null){
            listener?.onLogInfo("设备为空")
            return
        }
        val uuids = device.uuids
        device.fetchUuidsWithSdp()
        if (uuids == null) {
            listener?.onLogInfo("获取到的uuids 为空")
            return
        }
        stringBuilder.clear()
        stringBuilder.append("当前为")

        for (uuid in uuids) {
            socket = if (secure) {
                stringBuilder.append("加密模式")
                device.createRfcommSocketToServiceRecord(uuid.uuid)
            } else {
                stringBuilder.append("普通模式模式")
                device.createInsecureRfcommSocketToServiceRecord(uuid.uuid)
            }
            if (socket != null) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS)
                    condition.signalAll()
                    lock.unlock()
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener?.onDeviceStatusChange(STATUS_ERROR)
                }
            }
        }
        stringBuilder.append("bound state = ${device.bondState}")
        stringBuilder.append("type = ${device.type}")

        secure = !secure
    }

    fun createSocketService(device: BluetoothDevice) {
//        val server = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("heart", UUID);
//        val thread = Thread() {
//            kotlin.run {
//                while (true) {
//                    server?.accept()
//                }
//            }
//        }
    }


}