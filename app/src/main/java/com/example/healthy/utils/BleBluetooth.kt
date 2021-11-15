package com.example.healthy.utils

import android.app.Activity
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

object BleBluetooth : AbstractBluetooth() {
    private val TAG = BleBluetooth.javaClass.simpleName
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null


    private var scanCallBack: ScanCallback? = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.e(TAG, "onScanResult device = ${result?.device?.address}")
            listener?.onDeviceFound(result?.device)
        }
    }

    override fun deviceInit(application: Application) {
        scanner = bluetoothAdapter?.bluetoothLeScanner
    }

    override fun scanDevice(activity: Activity?) {
        scanner?.startScan(scanCallBack)
    }

    override fun stopScanDevice() {
        scanner?.stopScan(scanCallBack)
    }

    override fun connectDevice(context: Context?, device: BluetoothDevice?) {
        stopScanDevice()
        bluetoothGatt = device?.connectGatt(context?.applicationContext, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.e(TAG, "onConnectionStateChange")
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.e(TAG, "connected")
                    gatt?.discoverServices()
                    listener?.onDeviceStatusChange(newState)
                }

            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Log.e(TAG, "onServicesDiscovered status")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    listener?.onServiceFound(gatt?.services)
                    if(gatt?.services.isNullOrEmpty()){
                        listener?.onLogInfo("扫描服务为空")
                    }
                    for(server in gatt?.services!!){
                        connectService(server)
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.e(TAG, "onCharacteristicChanged")
                if (characteristic?.value != null) {
                    listener?.onDataReceive(characteristic.value, characteristic.value.size)
                }
            }
        })
    }

    override fun connectService(service: BluetoothGattService){
        for (character in service.characteristics) {
            bluetoothGatt?.setCharacteristicNotification(character, true)
        }
    }

    override fun destroy(activity: Activity?) {
        stopScanDevice()
    }

}