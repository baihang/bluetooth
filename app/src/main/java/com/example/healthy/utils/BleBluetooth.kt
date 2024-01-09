package com.example.healthy.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.healthy.MyApplication
import java.util.ArrayList

object BleBluetooth : AbstractBluetooth() {
    private val TAG = BleBluetooth.javaClass.simpleName
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var status = STATUS_NONE

    private var scanCallBack: ScanCallback? = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            listener?.onDeviceFound(result?.device)
        }
    }

    private val servicesList = ArrayList<BluetoothGattService>()

    private val myCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                servicesList.clear()
                if (ActivityCompat.checkSelfPermission(
                        MyApplication.globalContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt?.discoverServices()
                listener?.onDeviceStatusChange(newState)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                listener?.onDeviceStatusChange(STATUS_DESTROY)
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener?.onServiceFound(gatt?.services)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            listener?.onLogInfo("receive data size = ${characteristic?.value?.size}")
            if (status != STATUS_CONNECTED_SUCCESS) {
                listener?.onDeviceStatusChange(STATUS_CONNECTED_SUCCESS)
                status = STATUS_CONNECTED_SUCCESS
            }
            if (characteristic?.value != null) {
                listener?.onDataReceive(characteristic.value, characteristic.value.size)
            }
        }

    }

    override fun deviceInit(application: Application) {
        scanner = bluetoothAdapter?.bluetoothLeScanner
    }

    override fun scanDevice(activity: Activity?) {
        if (ActivityCompat.checkSelfPermission(
                MyApplication.globalContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        scanner?.startScan(scanCallBack)
    }

    override fun stopScanDevice() {
        if (ActivityCompat.checkSelfPermission(
                MyApplication.globalContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        scanner?.stopScan(scanCallBack)
    }

    override fun connectDevice(context: Context?, device: BluetoothDevice?){
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(
                    MyApplication.globalContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt?.close()
            bluetoothGatt = null
        }
        stopScanDevice()
        status = STATUS_NONE
        bluetoothGatt = device?.connectGatt(context?.applicationContext, false, myCallback)
    }

    override fun connectService(service: BluetoothGattService) {
        if (ActivityCompat.checkSelfPermission(
                MyApplication.globalContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        for (character in service.characteristics) {
            if (character.properties.and(BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                continue
            }
            val result = bluetoothGatt?.setCharacteristicNotification(character, true) ?: false
            if (result) {
                for (des in character.descriptors) {
                    des.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    bluetoothGatt?.writeDescriptor(des)
                }
            }
        }
    }

    override fun destroy(activity: Activity?) {
        stopScanDevice()
        listener?.onDeviceStatusChange(STATUS_DESTROY)
        if (ActivityCompat.checkSelfPermission(
                MyApplication.globalContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

}