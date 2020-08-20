package com.example.healthy.data

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class BluetoothBroadCastReceiver(context: Activity) : BroadcastReceiver(), LifecycleObserver {

    private var activity: Activity? = context

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun register() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        activity?.registerReceiver(this, filter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregister() {
        activity?.unregisterReceiver(this)
        activity = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(TAG, "intent action = ${intent?.action}")
        when (intent?.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (status) {
                    BluetoothAdapter.STATE_ON -> {
                        showToast("蓝牙已打开")
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        showToast("蓝牙已关闭")
                    }
                }
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {

            }
        }
    }

    companion object {
        private const val TAG = "BluetoothBroadCast"
    }

    private var toast:Toast? = null
    private fun showToast(str: String) {
        Log.e(TAG, "show toast $str")
        if(toast == null){
            toast = Toast.makeText(activity, str, Toast.LENGTH_LONG)
            toast?.show()
            return
        }
        toast?.setText(str)
        toast?.show()
    }

}