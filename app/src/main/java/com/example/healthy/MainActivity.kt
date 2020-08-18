package com.example.healthy

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.healthy.ui.main.DevicesFragment
import com.example.healthy.ui.main.DevicesViewModel

class MainActivity : AppCompatActivity() {

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothManager.adapter
    }

    private val module: DevicesViewModel by lazy {
        DevicesViewModel(application, bluetoothManager, bluetoothAdapter)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DevicesFragment.newInstance(module))
                .commitNow()
        }

        module.deviceLiveData.observe(this, Observer { devices ->
            Log.e(TAG, "devices size = ${devices.size}")
        })

        checkPermission()
    }

    override fun onStart() {
        super.onStart()
        if (!bluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, Companion.REQUEST_BT_ENABLE)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PERMISSION_GRANTED
        ) {
            requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_BT_ENABLE -> {
                Log.e(TAG, "onActivityResult REQUEST_BT_ENABLE")
            }
            REQUEST_LOCATION_PERMISSION -> {
                Log.e(TAG, "onActivityResult REQUEST_LOCATION_PERMISSION")
                if (resultCode != 0)
                    Toast.makeText(this, "不授权无法扫描蓝牙", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_BT_ENABLE = 1
        private const val REQUEST_LOCATION_PERMISSION = 2
        private const val TAG = "MainActivity"
    }
}