package com.example.healthy.ui.main

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.collection.ArraySet
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent

class DevicesViewModel(
    application: Application,
    mManager: BluetoothManager,
    mAdapter: BluetoothAdapter
) : AndroidViewModel(application) {

    private val TAG = "DevicesViewModel"

    private var value: Int = 0
    private var manager: BluetoothManager = mManager
    private val adapter: BluetoothAdapter = mAdapter
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    var scanning:MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     * 蓝牙设备列表
     */
    val deviceLiveData:MutableLiveData<ArraySet<BluetoothDevice>> by lazy {
       val liveData =  MutableLiveData<ArraySet<BluetoothDevice>>()
        liveData.value = ArraySet()
        liveData
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.e(TAG, "onScanResult device = ${result?.device?.address}")
            val list:ArraySet<BluetoothDevice> = deviceLiveData.value as ArraySet<BluetoothDevice>
            list.add(result?.device)
            deviceLiveData.value = list
        }
    }

    private val scanner: BluetoothLeScanner by lazy { adapter.bluetoothLeScanner }


    /**
     * 扫描设备
     */
    fun scanDevices(enable: Boolean) {
        Log.e(TAG, "scan devices = $enable")
        if (enable) {
            if(scanning.value == true){
                return
            }
            deviceLiveData.value?.clear()
            scanner.startScan(scanCallBack)
        } else {
            scanner.stopScan(scanCallBack)
        }
        scanning.value = enable
    }

    fun setDevices(device: BluetoothDevice) {
        this.device = device
        gatt = device.connectGatt(getApplication(), true, object : BluetoothGattCallback() {

        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause(){
        scanner.stopScan(scanCallBack)
        deviceLiveData.value?.clear()
    }

}