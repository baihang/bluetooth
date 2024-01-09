package com.example.healthy.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthy.R
import com.example.healthy.databinding.FragmentDevicesBinding
import com.example.healthy.utils.AbstractBluetooth
import com.example.healthy.utils.NoticePopWindow
import com.example.healthy.utils.SharedPreferenceUtil
import com.google.android.material.snackbar.Snackbar

/**
 * @author hang
 * 蓝牙相关
 */
class DevicesFragment : Fragment() {

    companion object {
        private const val TAG = "DevicesFragment"
    }

    private lateinit var binding: FragmentDevicesBinding
    private val model: DevicesViewModel by activityViewModels()

    private val rotateAnimator: RotateAnimation by lazy {
        val rotate = RotateAnimation(
            0F, 360F, Animation.RELATIVE_TO_SELF, 0.5F,
            Animation.RELATIVE_TO_SELF, 0.5F
        )
        rotate.duration = 500
        rotate.repeatCount = RotateAnimation.INFINITE
        rotate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.device_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e(TAG, "onOptionsItemSelected")
        if (item.itemId == R.id.device_refresh) {
            if (model.scanning.value == false) {
                adapter.deviceArray.clear()
                adapter.notifyDataSetChanged()
            }
            model.scanDevices(model.scanning.value == false, activity)
        } else if (item.itemId == R.id.device_change) {
            adapter.deviceArray.clear()
            adapter.notifyDataSetChanged()
            model.deviceChange(activity)
            binding.devicesToolbar.title = model.getDeviceType()
        }
        return super.onOptionsItemSelected(item)
    }

    private var adapter = DevicesAdapter()

    override fun onResume() {
        super.onResume()
        if (model.connectStatus.value ?: 0 < AbstractBluetooth.STATUS_CONNECTED_DEVICE) {
            model.scanDevices(true, activity)
        }
        upFragment = false
    }

    override fun onPause() {
        super.onPause()
        model.scanDevices(false, activity)
        strBuilder.clear()
    }

    private val strBuilder = StringBuilder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.devicesToolbar)
        setHasOptionsMenu(true)
        binding.devicesToolbar.title = model.getDeviceType()

        adapter.setItemClickListener(itemClickListener)
        binding.recycleView.adapter = adapter
        binding.recycleView.layoutManager = LinearLayoutManager(activity)

        binding.devicesToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
            (this as Fragment).onStop()
        }

        binding.deviceDataButton.setOnClickListener {
            //存储服务UUID
            val editor = SharedPreferenceUtil.getEditor(context)
            editor?.putString(model.device?.address, model.serviceUUID)
            editor?.apply()

            findNavController().navigateUp()
        }

        binding.virtualDeviceButton.setOnClickListener {
            model.openVirtualDevice()
            upFragment = true
        }

        model.resultValue.observe(viewLifecycleOwner, Observer { baseData ->
            debugInfo("收到数据：${baseData.label}")
            if (NoticePopWindow.isShow()) {
                NoticePopWindow.dismiss()
            }
        })

        model.deviceLiveData.observe(viewLifecycleOwner, Observer { set ->
            for (device in set) {
                if (!adapter.deviceArray.contains(device)) {
                    adapter.deviceArray.add(device)
                    debugInfo("设备： ${device?.address ?: "虚拟设备"}")
                }
            }
            adapter.notifyDataSetChanged()
        })

        model.scanning.observe(viewLifecycleOwner, Observer { scanning ->
            Log.e(TAG, "scanning = $scanning")
            debugInfo(
                "扫描状态更给 ： ${
                    if (scanning) {
                        "扫描中"
                    } else {
                        "扫描结束"
                    }
                }"
            )
            if (scanning) {
                Handler().postDelayed({
                    model.scanDevices(false, activity)
                }, 10000)
            }
        })

        model.connectStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                AbstractBluetooth.STATUS_SCANNING -> {
                    debugInfo("正在扫描设备")
                    adapter.notifyDataSetChanged()
                    binding.deviceDataButton.visibility = View.GONE
                }

                AbstractBluetooth.STATUS_CONNECTED_SUCCESS -> {
                    debugInfo("成功连接设备")
                    NoticePopWindow.dismiss()
                }

                AbstractBluetooth.STATUS_ERROR -> {
                    model.onDestroy(activity)
                    debugInfo("连接设备出错")
                }

                AbstractBluetooth.STATUS_RECEIVE_DATA -> {
                    if (upFragment) {
                        upFragment = false
                        findNavController().navigateUp()
                    }
                }

                AbstractBluetooth.STATUS_BLUETOOTH_CLOSE -> {
                    Snackbar.make(binding.devicesLayout, "打开蓝牙", Snackbar.LENGTH_LONG)
                        .setAction("打开", View.OnClickListener {
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                            }.launch(intent)
                        }).show()
                }
            }
        }

        model.logInfo.observe(viewLifecycleOwner, Observer {
            debugInfo(it)
        })

    }

    private var upFragment = false

    private fun debugInfo(str: String) {
        strBuilder.append(str).append("\n")
        binding.deviceDataEt.setText(strBuilder.toString())

        strBuilder.clear()
    }

    private val itemClickListener = object : OnItemClickListener {
        override fun onClickItem(position: Int, device: BluetoothDevice?) {
            NoticePopWindow.show(activity, binding.devicesLayout)
            debugInfo("连接中： " + device?.address ?: "虚拟设备")
            model.connectDevices(this@DevicesFragment.requireContext(), device)
        }
    }

    class DevicesAdapter() : RecyclerView.Adapter<DevicesViewHolder>() {
        val deviceArray: ArrayList<BluetoothDevice> = ArrayList()
        private var listener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.devices_list_item, parent, false
            )
            return DevicesViewHolder(view)
        }

        override fun getItemCount(): Int {
            return deviceArray.size
        }

        override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
            val device: BluetoothDevice? = deviceArray[position]
            kotlin.runCatching {
                holder.deviceName?.let {
                    it.text = device?.name ?: "蓝牙-未命名"
                }
            }

            holder.deviceMac?.text = device?.address ?: "mac address"

            holder.layout?.setOnClickListener {
                listener?.onClickItem(position, deviceArray.get(position))
            }
        }

        fun setItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }

    }

    class DevicesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var deviceName: TextView? = null
        var deviceMac: TextView? = null
        var layout: RelativeLayout? = null

        init {
            layout = itemView.findViewById(R.id.item_device_layout)
            deviceName = itemView.findViewById(R.id.item_device_name)
            deviceMac = itemView.findViewById(R.id.item_device_mac)
        }
    }

    interface OnItemClickListener {
        fun onClickItem(position: Int, device: BluetoothDevice?)
    }

}