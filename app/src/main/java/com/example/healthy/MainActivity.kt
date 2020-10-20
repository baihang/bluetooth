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
import com.example.healthy.data.BluetoothBroadCastReceiver
import com.example.healthy.ui.main.DevicesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var manager: BluetoothManager

    private lateinit var adapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModelFactory.create(DevicesViewModel::class.java)
        manager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = manager.adapter
        checkPermission()
        this.lifecycle.addObserver(BluetoothBroadCastReceiver(this))
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

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 2
        private const val TAG = "MainActivity"
    }
}