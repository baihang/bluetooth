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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthy.ui.main.DevicesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var manager: BluetoothManager

    private lateinit var adapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        val model = viewModelFactory.create(DevicesViewModel::class.java)
        manager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = manager.adapter
        checkPermission()
    }

    override fun onStart() {
        super.onStart()
        if (!adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            }.launch(intent)
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