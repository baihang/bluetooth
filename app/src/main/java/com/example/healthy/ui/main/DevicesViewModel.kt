package com.example.healthy.ui.main

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.collection.ArraySet
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent

class DevicesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var manager: BluetoothManager? = null

    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null

    private fun initManager() {
        if (manager == null) {
            manager =
                getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        }
        adapter = manager?.adapter
        scanner = adapter?.bluetoothLeScanner
    }


    private var value: Int = 0
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    var scanning: MutableLiveData<Boolean> = MutableLiveData(false)
    var noticeMsg: MutableLiveData<String> = MutableLiveData()

    /**
     * 蓝牙设备列表
     */
    val deviceLiveData: MutableLiveData<ArraySet<BluetoothDevice>> by lazy {
        val liveData = MutableLiveData<ArraySet<BluetoothDevice>>()
        liveData.value = ArraySet()
        liveData
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.e(TAG, "onScanResult device = ${result?.device?.address}")
            val list: ArraySet<BluetoothDevice> = deviceLiveData.value as ArraySet<BluetoothDevice>
            list.add(result?.device)
            deviceLiveData.value = list
        }
    }

    /**
     * 扫描设备
     */
    fun scanDevices(enable: Boolean) {
        if (scanner == null) {
            initManager()
        }
        if (adapter?.isEnabled != true) {
            noticeMsg.value = "蓝牙已关闭，请打开蓝牙"
            return
        }
        if (enable) {
            if (scanning.value == true) {
                return
            }
            deviceLiveData.value?.clear()
            scanner?.startScan(scanCallBack)
        } else {
            scanner?.stopScan(scanCallBack)
        }
        scanning.value = enable
    }

    fun setDevices(device: BluetoothDevice) {
        this.device = device
        gatt = device.connectGatt(getApplication(), true, object : BluetoothGattCallback() {
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        scanner?.stopScan(scanCallBack)
        deviceLiveData.value?.clear()
    }

    companion object {
        private const val TAG = "DevicesViewModel"
    }

}