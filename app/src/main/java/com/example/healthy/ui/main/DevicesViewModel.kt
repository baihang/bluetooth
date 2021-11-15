package com.example.healthy.ui.main

import android.app.Activity
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
import com.example.healthy.utils.*
import java.lang.StringBuilder
import java.util.concurrent.LinkedBlockingQueue

class DevicesViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DevicesViewModel"
    }

    private var timeStamps: ArrayList<Long> = ArrayList(10)

    /**
     * true ble
     * false normal
     */
    private var bleOrNormal = false
    private var bluetooth: AbstractBluetooth? = null

    private var value: Int = 0
    var device: BluetoothDevice? = null
    var serviceUUID: String? = null

    private val dataAnalyzer: DataAnalyze by lazy { DataAnalyze() }

    var scanning: MutableLiveData<Boolean> = MutableLiveData(false)
    var noticeMsg: MutableLiveData<String> = MutableLiveData()
    var serviceList: MutableLiveData<List<BluetoothGattService>> = MutableLiveData(ArrayList())

    private var characteristicList: MutableLiveData<List<BluetoothGattCharacteristic>> =
        MutableLiveData(ArrayList())

    var connectStatus: MutableLiveData<Int> = MutableLiveData(BluetoothAdapter.STATE_DISCONNECTED)

    var resultValue: MutableLiveData<BaseData> = MutableLiveData()

    var logInfo: MutableLiveData<String> = MutableLiveData()

    var dataQueue = LinkedBlockingQueue<BaseData>()

    var timeStampLive: MutableLiveData<Long> = MutableLiveData()
    private var timeStampCount = 0L

    private val DEBUG = false
    private val isUploadData = false

    private fun initManager() {
        bluetooth = if (bleOrNormal) {
            BleBluetooth
        } else {
            NormalBluetooth
        }
        if(DEBUG){
            bluetooth = VirtualBluetooth
        }
        bluetooth?.baseInit(getApplication())
        AbstractBluetooth.listener = object : AbstractBluetooth.BluetoothListener {
            override fun onDeviceFound(device: BluetoothDevice?) {
                Log.e(TAG, "device found = ${device?.address}")
                val list = deviceLiveData.value
                list?.add(device)
                deviceLiveData.postValue(list)
            }

            override fun onDeviceStatusChange(status: Int) {
                connectStatus.postValue(status)
            }

            override fun onDataReceive(bytes: ByteArray, len: Int) {
                receivedData(bytes.copyOfRange(0, len))
                connectStatus.postValue(len)
            }

            override fun onLogInfo(log: String) {
                logInfo.postValue(log)
            }

        }

        ThreadUtil.getInstance()?.addThread(uploadThread)
    }

    fun deviceChange(activity: Activity?) {
        bluetooth?.destroy(activity)
        bleOrNormal = !bleOrNormal
        initManager()
        scanDevices(true, activity)
    }

    fun getDeviceType(): String {
        return when (bluetooth) {
            is NormalBluetooth -> "普通蓝牙"
            is BleBluetooth -> "BLE蓝牙"
            else -> if (bleOrNormal) "BLE蓝牙" else "普通蓝牙"
        }
    }

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

    /**
     * 扫描设备
     */
    fun scanDevices(enable: Boolean, activity: Activity?) {
        if (scanning.value == enable) {
            return
        }
        if (bluetooth == null) {
            initManager()
        }
        if (AbstractBluetooth.bluetoothAdapter?.isEnabled != true) {
            noticeMsg.value = "蓝牙已关闭，请打开蓝牙"
            return
        }
        if (enable) {
            deviceLiveData.value?.clear()
            connectStatus.postValue(BluetoothAdapter.STATE_DISCONNECTED)
            bluetooth?.scanDevice(activity)
        } else {
            bluetooth?.stopScanDevice()
        }
        scanning.value = enable
    }

    fun connectService(service: BluetoothGattService) {
        serviceUUID = service.uuid.toString()
        characteristicList.value = service.characteristics
        bluetooth?.connectService(service)
    }

    fun connectDevices(device: BluetoothDevice?) {
        bluetooth?.stopScanDevice()
        bluetooth?.connectDevice(null, device)
    }

    private fun receivedData(bytes: ByteArray) {
        val result = dataAnalyzer.parseData(bytes)
        if (result != null) {
            resultValue.postValue(result!!)
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

    public fun testUpload(result: BaseData) {
        dataQueue.add(result)
    }

    private val strBuilder = StringBuilder()

    private val uploadThread = Runnable {
        kotlin.run {
            while (isUploadData) {
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
                    if (count >= 200) {
                        strBuilder.append(d.timeStamp).append("\n")
                        count = 0
                    }
                }
                if(dates.isEmpty()){
                    strBuilder.append(data.timeStamp)
                }else{
                    strBuilder.append((dates[dates.size - 1] as BaseData).timeStamp)
                }
                val result = NetWortUtil.upEcgData(strBuilder.toString())
                if (result.isSucceed) {
//                    Log.e(TAG, "upload result = $result param = ${strBuilder.toString()}")
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        bluetooth?.stopScanDevice()
        deviceLiveData.value?.clear()
    }

}