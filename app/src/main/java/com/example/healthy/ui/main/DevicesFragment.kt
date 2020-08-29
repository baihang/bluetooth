package com.example.healthy.ui.main

import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthy.R
import com.example.healthy.databinding.FragmentDevicesBinding
import com.google.android.material.snackbar.Snackbar

/**
 * @author hang
 * 蓝牙相关
 */
class DevicesFragment : Fragment() {

    companion object {
        private const val TAG = "DevicesFragment"

        private const val LIST_MODEL_DEVICES = 0
        private const val LIST_MODEL_SERVICE = 1
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DevicesAdapter()
        adapter.setItemClickListener(itemClickListener)
        binding.recycleView.adapter = adapter
        binding.recycleView.layoutManager = LinearLayoutManager(activity)

        binding.devicesToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.devicesRefresh.setOnClickListener {
            if (model.scanning.value == false) {
                adapter.deviceArray.clear()
                adapter.notifyDataSetChanged()
            }
            model.scanDevices(model.scanning.value == false)
        }

        model.deviceLiveData.observe(viewLifecycleOwner, Observer { set ->
            for (device in set) {
                if (!adapter.deviceArray.contains(device)) {
                    adapter.deviceArray.add(device)
                }
            }
            adapter.notifyDataSetChanged()
        })

        model.scanning.observe(viewLifecycleOwner, Observer { scanning ->
            Log.e(TAG, "scanning = $scanning")
            if (scanning) {
                Handler().postDelayed({
                    model.scanDevices(false)
                }, 10000)
                binding.devicesRefresh.startAnimation(rotateAnimator)

            } else {
                binding.devicesRefresh.clearAnimation()
            }
        })


        model.connectStatus.observe(viewLifecycleOwner, { status ->
            when (status) {
                BluetoothAdapter.STATE_DISCONNECTED->{
                    adapter.listMode = LIST_MODEL_DEVICES
                    adapter.notifyDataSetChanged()
                }

                BluetoothAdapter.STATE_CONNECTED -> {
                    adapter.listMode = LIST_MODEL_SERVICE
                    adapter.notifyDataSetChanged()
                    model.discoversService()
                }

                DevicesViewModel.SERVICE_CONNECTED ->{
                    Log.e(TAG, "service connected")
                }
            }
        })

        model.noticeMsg.observe(viewLifecycleOwner, {
            Snackbar.make(binding.devicesLayout, model.noticeMsg.value ?: "", Snackbar.LENGTH_LONG)
                .setAction("打开", View.OnClickListener {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    }.launch(intent)
                }).show()
        })

        model.serviceList.observe(viewLifecycleOwner, {
//            adapter.listMode = LIST_MODEL_SERVICE
            for (service in it) {
                adapter.serviceList.add(service)
            }
            adapter.notifyDataSetChanged()
        })
    }

    private val itemClickListener = object : OnItemClickListener {
        override fun onClickItem(position: Int, device: BluetoothDevice) {
            Log.e(TAG, "click listener position = $position")
            model.connectDevices(device)
        }

        override fun connectService(position: Int, service: BluetoothGattService) {

        }

    }

    class DevicesAdapter() : RecyclerView.Adapter<DevicesViewHolder>() {
        val deviceArray: ArrayList<BluetoothDevice> = ArrayList()
        val serviceList: ArrayList<BluetoothGattService> = ArrayList()
        var listMode = LIST_MODEL_DEVICES
        private var listener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.devices_list_item, parent, false
            )
            return DevicesViewHolder(view)
        }

        override fun getItemCount(): Int {
            return if (listMode == LIST_MODEL_DEVICES) {
                deviceArray.size
            } else {
                serviceList.size
            }

        }

        override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
            if (listMode == LIST_MODEL_DEVICES) {
                val device: BluetoothDevice? = deviceArray[position]
                holder.deviceName?.text = device?.name ?: "蓝牙-未命名"
                holder.deviceMac?.text = device?.address ?: "mac address"
            } else {
                val service = serviceList[position]
                holder.deviceName?.text = service.uuid.toString()
            }

            holder.layout?.setOnClickListener {
                if (listMode == LIST_MODEL_DEVICES) {
                    listener?.onClickItem(position, deviceArray[position])
                } else {
                    listener?.connectService(position, serviceList[position])
                }
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
        fun onClickItem(position: Int, device: BluetoothDevice)
        fun connectService(position: Int, service: BluetoothGattService)
    }

}