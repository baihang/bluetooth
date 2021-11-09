package com.example.healthy.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class NormalBluetooth {

    private val bluetoothAdapter: BluetoothAdapter? = null
    private val TAG = "NormalBluetooth"

    fun init() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device:BluetoothDevice? =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val pair = Pair<String?, String?>(device?.name, device?.address)
                    Log.e(TAG, "name = ${pair.first} addr = ${pair.second}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {

                }
            }
        }
    }

    fun scanDevice(activity: Activity) {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity.registerReceiver(receiver, intentFilter)
        bluetoothAdapter?.startDiscovery()

    }

    fun stopScanDevice(activity: Activity) {
        activity.unregisterReceiver(receiver)
    }

    fun connectDevice(device: BluetoothDevice){
        bluetoothAdapter?.cancelDiscovery()
//        device.createRfcommSocketToServiceRecord()
    }

}