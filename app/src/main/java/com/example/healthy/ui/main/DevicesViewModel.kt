package com.example.healthy.ui.main

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.net.wifi.aware.Characteristics
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.collection.ArraySet
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.example.healthy.bean.AbstractLoadBean
import com.example.healthy.bean.NetworkBean
import com.example.healthy.data.BaseData
import com.example.healthy.data.DataAnalyze
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.RxManagerUtil
import com.example.healthy.utils.ThreadUtil
import java.lang.StringBuilder
import java.util.concurrent.LinkedBlockingQueue

class DevicesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var manager: BluetoothManager? = null

    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var timeStamps: ArrayList<Long> = ArrayList(10)

    private fun initManager() {
        if (manager == null) {
            manager =
                getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        }
        adapter = manager?.adapter
        scanner = adapter?.bluetoothLeScanner

        ThreadUtil.getInstance()?.addThread(uploadThread)
    }


    private var value: Int = 0
    var device: BluetoothDevice? = null
    var serviceUUID: String? = null
    private var gatt: BluetoothGatt? = null

    private val dataAnalyzer: DataAnalyze by lazy { DataAnalyze() }

    var scanning: MutableLiveData<Boolean> = MutableLiveData(false)
    var noticeMsg: MutableLiveData<String> = MutableLiveData()
    var serviceList: MutableLiveData<List<BluetoothGattService>> = MutableLiveData(ArrayList())

    private var characteristicList: MutableLiveData<List<BluetoothGattCharacteristic>> =
        MutableLiveData(ArrayList())

    var connectStatus: MutableLiveData<Int> = MutableLiveData(BluetoothAdapter.STATE_DISCONNECTED)

    var resultValue: MutableLiveData<BaseData> = MutableLiveData()

    var dataQueue = LinkedBlockingQueue<BaseData>()

    var timeStampLive: MutableLiveData<Long> = MutableLiveData()
    private var timeStampCount = 0L

    /**
     * 测试时间戳
     */
    fun testTime(result: BaseData) {
        when {
            timeStamps.size < 10 -> {
                timeStamps.add(result.timeStamp)
            }
            timeStamps.size == 10 -> {
                timeStamps.removeAt(0)
                timeStamps.add(result.timeStamp)
                timeStampLive.postValue(timeStamps[9] - timeStamps[0])
            }
            else -> {
                timeStamps.clear()
            }
        }
    }

    /**
     * 蓝牙设备列表
     */
    val deviceLiveData: MutableLiveData<ArraySet<BluetoothDevice>> by lazy {
        val liveData = MutableLiveData<ArraySet<BluetoothDevice>>()
        liveData.value = ArraySet()
        liveData
    }

    private var scanCallBack: ScanCallback? = object : ScanCallback() {
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
        if(scanning.value == enable){
            return
        }
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
            connectStatus.postValue(BluetoothAdapter.STATE_DISCONNECTED)
            scanner?.startScan(scanCallBack)
        } else {
            scanner?.stopScan(scanCallBack)
        }
        scanning.value = enable
    }

    fun connectService(service: BluetoothGattService) {
        serviceUUID = service.uuid.toString()
        characteristicList.value = service.characteristics
        connectStatus.postValue(SERVICE_CONNECTED)

        Log.e(TAG, "service connected character = ${service.characteristics.size}")
        for (character in service.characteristics) {
            gatt?.setCharacteristicNotification(character, true)
        }
    }

    fun connectDevices(device: BluetoothDevice) {
        scanDevices(false)
        this.device = device
        gatt = device.connectGatt(getApplication(), true, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.e(TAG, "onConnectionStateChange")
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.e(TAG, "connected")
                    gatt?.discoverServices()
                    connectStatus.postValue(newState)
                }

            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Log.e(TAG, "onServicesDiscovered status")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    serviceList.postValue(gatt?.services)
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.e(TAG, "onCharacteristicChanged")
                if (characteristic?.value != null) {
                    val result = dataAnalyzer.parseData(characteristic.value)
                    if (result != null) {
                        resultValue.postValue(result)
                        dataQueue.add(result.clone())
                        when {
                            timeStamps.size < 10 -> {
                                timeStamps.add(result.timeStamp)
                            }
                            timeStamps.size == 10 -> {
                                timeStamps.removeAt(0)
                                timeStamps.add(result.timeStamp)
                                timeStampLive.postValue(timeStamps[9] - timeStamps[0])
                            }
                            else -> {
                                timeStamps.clear()
                            }
                        }
                    }
                }
            }
        })
    }

    public fun testUpload(result: BaseData) {
        dataQueue.add(result)
    }

    private val strBuilder = StringBuilder()

    private val uploadThread = Runnable {

        kotlin.run {
            while (true) {
                val data = dataQueue.take();
                Thread.sleep(10 * 1000)
                strBuilder.clear()
                strBuilder.append(data.getUploadLabel())
                strBuilder.append("\n")
                strBuilder.append(data.timeStamp)
                strBuilder.append("\n")
                strBuilder.append(data.getBodyData())
                val dates = dataQueue.toArray()
                dataQueue.clear()
                var count = 1
                for (d in dates) {
                    strBuilder.append((d as BaseData).getBodyData())
                    count++
                    if(count >= 200){
                        strBuilder.append(d.timeStamp).append("\n")
                        count = 0
                    }
                }
                strBuilder.append((dates[dates.size-1] as BaseData).timeStamp)
                val result = NetWortUtil.upEcgData(strBuilder.toString())
                if (result.isSucceed) {
//                    Log.e(TAG, "upload result = $result param = ${strBuilder.toString()}")
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        scanner?.stopScan(scanCallBack)
        deviceLiveData.value?.clear()
    }

    companion object {
        private const val TAG = "DevicesViewModel"
        const val SERVICE_CONNECTED = 3

    }

}