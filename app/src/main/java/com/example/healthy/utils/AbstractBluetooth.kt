package com.example.healthy.utils

import android.app.Activity
import android.app.Application
import android.bluetooth.*
import android.content.Context

abstract class AbstractBluetooth {

    companion object {
        const val STATUS_CONNECTED_DEVICE = BluetoothGatt.STATE_CONNECTED
        const val STATUS_CONNECTED_SUCCESS = 3
        const val STATUS_DESTROY = 4
        const val STATUS_ERROR = -1

        var bluetoothAdapter: BluetoothAdapter? = null
        var manager: BluetoothManager? = null
        var listener: BluetoothListener? = null
    }


    fun baseInit(application: Application) {
        if (manager == null) {
            manager =
                application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        }
        bluetoothAdapter = manager?.adapter
        deviceInit(application)
    }

    protected abstract fun deviceInit(application: Application)

    abstract fun scanDevice(activity: Activity?)

    abstract fun stopScanDevice()

    abstract fun connectDevice(context: Context?, device: BluetoothDevice?)

    open fun connectService(service: BluetoothGattService){}

    abstract fun destroy(activity: Activity?)

    interface BluetoothListener {
        fun onDeviceFound(device: BluetoothDevice?)
        fun onDeviceStatusChange(status: Int)
        fun onDataReceive(bytes: ByteArray, len: Int)
        fun onServiceFound(services: List<BluetoothGattService>?) {}
        fun onLogInfo(log: String){}
    }

}