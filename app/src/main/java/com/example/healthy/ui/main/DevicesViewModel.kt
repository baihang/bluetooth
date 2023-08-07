package com.example.healthy.ui.main

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.aware.Characteristics
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.collection.ArraySet
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.example.healthy.MainActivity
import com.example.healthy.MyApplication
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
    private var bleOrNormal = true
    private val DEBUG = false
    private val isUploadData = true
    private var bluetooth: AbstractBluetooth? = null

    private var value: Int = 0
    var device: BluetoothDevice? = null
    var serviceUUID: String? = null

    private val dataAnalyzer: DataAnalyze by lazy { DataAnalyze() }

    var scanning: MutableLiveData<Boolean> = MutableLiveData(false)

    private var characteristicList: MutableLiveData<List<BluetoothGattCharacteristic>> =
        MutableLiveData(ArrayList())

    var connectStatus: MutableLiveData<Int> = MutableLiveData(BluetoothAdapter.STATE_DISCONNECTED)

    var resultValue: MutableLiveData<BaseData> = MutableLiveData()

    var logInfo: MutableLiveData<String> = MutableLiveData()

    var dataQueue = LinkedBlockingQueue<BaseData>()

    var timeStampLive: MutableLiveData<Long> = MutableLiveData()
    private var timeStampCount = 0L

    private fun initManager() {
        bluetooth = if (bleOrNormal) {
            BleBluetooth
        } else {
            NormalBluetooth
        }
        if (DEBUG) {
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

            override fun onServiceFound(services: List<BluetoothGattService>?) {
                if (services.isNullOrEmpty()) {
                    logInfo.postValue("onServiceFound null or empty")
                    return
                }
                for (s in services) {
                    bluetooth?.connectService(s)
                }
            }

            override fun onLogInfo(log: String) {
                logInfo.postValue(log)
            }

        }

        ThreadUtil.getInstance()?.addThread(uploadThread)
    }

    private var i = 1
    private val stringBuilder = StringBuilder()
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
            logInfo.postValue("蓝牙已关闭，请打开蓝牙")
            connectStatus.postValue(AbstractBluetooth.STATUS_BLUETOOTH_CLOSE)
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

    fun connectDevices(context: Context, device: BluetoothDevice?) {
        bluetooth?.stopScanDevice()
        bluetooth?.connectDevice(context, device)
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
                val title = strBuilder.toString()
                strBuilder.clear()
                strBuilder.append(data.getBodyData())
                val dates = dataQueue.toArray()
                dataQueue.clear()
                var count = 1
                for (d in dates) {
                    strBuilder.append((d as BaseData).getBodyData())
                    count++
                    if (count >= 200 && dates.size > 200) {
                        strBuilder.append(d.timeStamp).append("\n")
                        count = 0
                    }
                }
                if (dates.isEmpty()) {
                    strBuilder.append(data.timeStamp)
                } else {
                    strBuilder.append((dates[dates.size - 1] as BaseData).timeStamp)
                }
                NetWortUtil.upEcgData(title + strBuilder.toString())
            }
        }
    }

    private fun uploadLocation(){
        if(location != null){

        }
    }

    private var location: Location? = null
    private val locationListener by lazy {
        object :
            LocationListener {
            override fun onLocationChanged(l: Location?) {
                Log.e(TAG, "onLocationChanged :${l?.toString()}")
                location = l
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String?) {

            }

            override fun onProviderDisabled(provider: String?) {
            }

        }
    }

    private val locationManager by lazy { getApplication<MyApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        Log.e(TAG, "on resume ")
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000,
            100F,
            locationListener
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        Log.e(TAG, "on pause ")
        bluetooth?.stopScanDevice()
        deviceLiveData.value?.clear()
        locationManager.removeUpdates(locationListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(activity: Activity?) {
        Log.e(TAG, "on destroy")
        bluetooth?.destroy(activity)
    }

}