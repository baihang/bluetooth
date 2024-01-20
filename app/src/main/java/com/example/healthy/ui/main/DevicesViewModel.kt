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
import android.content.res.ColorStateList
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.example.healthy.MainActivity
import com.example.healthy.MyApplication
import com.example.healthy.R
import com.example.healthy.bean.AbstractLoadBean
import com.example.healthy.bean.HistoryFile
import com.example.healthy.bean.NetworkBean
import com.example.healthy.bean.Status
import com.example.healthy.bean.UploadEcgBean
import com.example.healthy.data.BaseData
import com.example.healthy.data.DataAnalyze
import com.example.healthy.db.AbstractAppDataBase
import com.example.healthy.db.dao.HistoryFileDao
import com.example.healthy.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.util.concurrent.LinkedBlockingQueue

class DevicesViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DevicesViewModel"

        @Volatile
        private var isUploadData = false

    }

    private var timeStamps: ArrayList<Long> = ArrayList(10)

    /**
     * true ble
     * false normal
     */
    private var bleOrNormal = true
    private var bluetooth: AbstractBluetooth? = null

    private var value: Int = 0
    var device: BluetoothDevice? = null
    var serviceUUID: String? = null

    private val dataAnalyzer: DataAnalyze by lazy { DataAnalyze() }

    var scanning: MutableLiveData<Boolean> = MutableLiveData(false)

    private var characteristicList: MutableLiveData<List<BluetoothGattCharacteristic>> =
        MutableLiveData(ArrayList())

    var connectStatus: MutableLiveData<Int> = MutableLiveData(BluetoothAdapter.STATE_DISCONNECTED)
    var dataStatus: MutableLiveData<DATA_STATUS> = MutableLiveData(DATA_STATUS.NO_DEVICE)

    var resultValue: MutableLiveData<BaseData> = MutableLiveData()

    var logInfo: MutableLiveData<String> = MutableLiveData()

    var dataQueue = LinkedBlockingQueue<BaseData>()

    var timeStampLive: MutableLiveData<Long> = MutableLiveData()
    private var timeStampCount = 0L

    val fileDao: HistoryFileDao by lazy {
        AbstractAppDataBase.getInstance(application).historyDao()
    }

    private fun initManager() {
        bluetooth = if (bleOrNormal) {
            BleBluetooth
        } else {
            NormalBluetooth
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
                if (status == AbstractBluetooth.STATUS_CONNECTED_SUCCESS) {
                    dataStatus.postValue(DATA_STATUS.DATA)
                }
            }

            override fun onDataReceive(bytes: ByteArray, len: Int) {
                receivedData(bytes.copyOfRange(0, len))
                connectStatus.postValue(AbstractBluetooth.STATUS_RECEIVE_DATA)
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
        if (!isUploadData) {
            isUploadData = true
            ThreadUtil.getInstance()?.addThread(uploadThread)
        }
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

    fun openVirtualDevice() {
        initManager()
        VirtualBluetooth.let {
            it.baseInit(getApplication())
            it.scanDevice(null)
            it.connectDevice(null, null)
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
        if (pause) return
        val result = dataAnalyzer.parseData(bytes)
        result?.let {
            resultValue.postValue(it)
            dataQueue.add(it.clone())
            fileOutputStream?.apply {
                if (!closeOutput) {
                    write(it.getDataString().toByteArray())
                }
            }
            when {
                timeStamps.size < 10 -> {
                    timeStamps.add(it.timeStamp)
                }

                timeStamps.size == 10 -> {
                    timeStamps.removeAt(0)
                    timeStamps.add(it.timeStamp)
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
                val data = dataQueue.take()
                Thread.sleep(1000)
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
                for (d in dates) {
                    (d as BaseData).getDataString(strBuilder)
                }
                uploadEcgMinute(strBuilder.toString())
//                NetWortUtil.upEcgData(strBuilder.toString())
            }
        }
    }

    private val ecgMinuteList = ArrayList<String>()
    private fun uploadEcgMinute(data: String) {
        ecgMinuteList.add(data)
        if (ecgMinuteList.size < 3) {
            return
        }
        loge("ecgMinuteList size = ${ecgMinuteList.size}")
        val file =
            LocalFileUtil.createFile(MyApplication.globalContext, "heart", "temp$tempIndex.txt")
        tempIndex++
        if (tempIndex > 10) {
            tempIndex = 0
        }
        file?.let {
            it.createNewFile()
            val outputStream = file.outputStream()
            for (str in ecgMinuteList) {
                outputStream.write(str.toByteArray())
            }
            val map = HashMap<String, Any>()
            map["data"] = file
            CoroutineScope(Dispatchers.IO).launch {
//                val result = NetWortUtil.postMulti("http://www.vipmember.com.cn:82/analyse", map)
                val result = NetWortUtil.postMulti("http://www.vipmember.com.cn:81/getHeartRate", map)
                loge("result = $result")
                if(result.isSucceed && result.data.isNotEmpty()){
//                    val res = result.data.replace("\\", "")
//                    val bean = JsonUtil.jsonStr2Object(res.substring(1, res.length - 1), UploadEcgBean::class.java)
//                    uploadNetResult.postValue("通道：${bean.channel} 心率：${bean.heartRate} 分析：${bean.result}")
                    uploadNetResult.postValue("接口请求成功：${result.data}")
                }else{
                    uploadNetResult.postValue("请求错误：${result.data}")
                }
            }
        }
        ecgMinuteList.clear()
    }
    val uploadNetResult: MutableLiveData<String> = MutableLiveData("")
    private var tempIndex = 0

    private fun uploadLocation() {
        if (location != null) {

        }
    }

    val stringBuilder = StringBuilder()
    var filePath: String? = null
    private var outPutStream: FileOutputStream? = null
    fun saveToFile(context: Context?): String? {
        context ?: return null
        Log.e(TAG, "save to file")
        var value = ""
        synchronized(stringBuilder) {
            value = stringBuilder.toString()
            stringBuilder.clear()
        }
//        val result = "${LocalFileUtil.getDateStr()}\n$value\n"
        val result = "$value "
        if (outPutStream == null) {
            val file =
                LocalFileUtil.createFile(context, "heart", "${LocalFileUtil.getDateStr()}.txt")
            filePath = file?.absolutePath
            Log.e(TAG, "open file = ${file?.absolutePath}")
            outPutStream = FileOutputStream(file)
        }
        outPutStream?.write(result.toByteArray())
        outPutStream?.flush()
        return filePath
    }

    var fileOutputStream: FileOutputStream? = null
    fun openFileOutStream(context: Context?): String? {
        closeOutput = false

        val file =
            LocalFileUtil.createFile(context, "heart", "${LocalFileUtil.getDateStr()}.txt")
        file?.let {
            val path = it.absolutePath
            Log.e(TAG, "open file = ${it.absolutePath}")
            kotlin.runCatching {
                fileOutputStream = FileOutputStream(it)
                return path
            }
        }

        return null
    }

    @Volatile
    private var closeOutput = false

    fun closeFileOutStream() {
        closeOutput = true
        fileOutputStream?.close()
        fileOutputStream = null
    }

    fun closeFile() {
        outPutStream?.close()
        outPutStream = null
        stringBuilder.clear()
    }

    fun uploadEcg(filePath: String): NetworkBean<String>? {
        return null
    }

    private var location: Location? = null
    private val locationListener by lazy {
        object :
            LocationListener {
            override fun onLocationChanged(l: Location) {
                Log.e(TAG, "onLocationChanged :${l.toString()}")
                location = l
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String) {

            }

            override fun onProviderDisabled(provider: String) {
            }

        }
    }

    private val locationManager by lazy { getApplication<MyApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
//        Log.e(TAG, "on resume ")
//        locationManager.requestLocationUpdates(
//            LocationManager.GPS_PROVIDER,
//            10000,
//            100F,
//            locationListener
//        )
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

    fun dataPause(pause: Boolean) {
        this.pause = pause
        dataStatus.postValue(if (pause) DATA_STATUS.PAUSE else DATA_STATUS.DATA)
    }

    private var pause = false

    enum class DATA_STATUS {
        NO_DEVICE,
        DATA,
        PAUSE
    }

}