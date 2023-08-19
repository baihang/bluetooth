package com.example.healthy

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.healthy.data.BluetoothBroadCastReceiver
import com.example.healthy.ui.main.DevicesViewModel
import com.example.healthy.utils.ActivityHook
import com.example.healthy.utils.SharedPreferenceUtil

class MainActivity : AppCompatActivity() {

    private lateinit var manager: BluetoothManager
    private lateinit var adapter: BluetoothAdapter

    private val shared: SharedPreferences? by lazy { SharedPreferenceUtil.getSharedPreference(this) }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

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

        if (shared?.getBoolean("isLogin", false) == false) {
            findNavController(R.id.nav_host_fragment).navigate(R.id.LoginBySmsFragment)
        }else{
            findNavController(R.id.nav_host_fragment).navigate(R.id.DataFragment)
        }

        //test
//        val intent = Intent(this, CameraActivity::class.java);
//        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        if (!adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            }.launch(intent)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        ActivityHook.replaceFullIns()
//        ActivityHook.replaceInstrumentation(this)
    }

    private fun checkPermission() {
        for (permission in permissions){
            if (ContextCompat.checkSelfPermission(this, permission)
                != PERMISSION_GRANTED
            ) {
                requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_LOCATION_PERMISSION
                )
            }

        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 2
        private const val TAG = "MainActivity"
    }
}