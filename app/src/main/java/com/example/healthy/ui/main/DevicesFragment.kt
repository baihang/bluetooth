package com.example.healthy.ui.main

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.Adapter
import android.widget.ListAdapter
import android.widget.TextView
import androidx.collection.ArraySet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthy.MainActivity
import com.example.healthy.R
import com.example.healthy.databinding.DevicesListItemBinding
import com.example.healthy.databinding.FragmentDevicesBinding

/**
 * @author hang
 * 蓝牙相关
 */
class DevicesFragment private constructor(viewModel: DevicesViewModel) : Fragment() {

    companion object {
        fun newInstance(viewModel: DevicesViewModel): DevicesFragment {
            return DevicesFragment(viewModel)
        }

        private const val TAG = "DevicesFragment"
    }

    private lateinit var binding: FragmentDevicesBinding
    private val model: DevicesViewModel = viewModel

    private val rotateAnimator: RotateAnimation by lazy {
        val view = binding.devicesRefresh
        val rotate = RotateAnimation(
            0F, 360F, view.width / 2F, view.height / 2F
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
        binding.recycleView.adapter = adapter
        binding.recycleView.layoutManager = LinearLayoutManager(activity)

        binding.devicesRefresh.setOnClickListener {
            adapter.deviceArray.clear()
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

    }

    class DevicesAdapter() : RecyclerView.Adapter<DevicesViewHolder>() {
        val deviceArray: ArrayList<BluetoothDevice> = ArrayList()

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
            holder.deviceName?.text = device?.name ?: "蓝牙-未命名"
            holder.deviceMac?.text = device?.address ?: "mac address"
        }

    }

    class DevicesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var deviceName: TextView? = null
        var deviceMac: TextView? = null

        init {
            deviceName = itemView.findViewById(R.id.item_device_name)
            deviceMac = itemView.findViewById(R.id.item_device_mac)
        }
    }

}