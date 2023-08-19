package com.example.healthy.ui.main

import android.bluetooth.BluetoothAdapter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.healthy.R
import com.example.healthy.chart.MyLineChart
import com.example.healthy.data.BaseData
import com.example.healthy.databinding.FragmentDataBinding
import com.example.healthy.utils.SharedPreferenceUtil
import com.example.healthy.utils.TokenRefreshUtil

class DataFragment : Fragment() {

    private lateinit var binding: FragmentDataBinding
    private val viewModel: DevicesViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        val sp = SharedPreferenceUtil.getSharedPreference(context)
        val xMax =
            sp?.getInt(SettingViewModel.X_MAX, MyLineChart.MAX_X_LENGTH) ?: MyLineChart.MAX_X_LENGTH
        binding.lineChart.initDataSet("心电 #1", Color.RED, xMax)

        binding.userId.text = resources.getString(R.string.user_id, TokenRefreshUtil.getUserId(context))

        viewModel.dataStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                DevicesViewModel.DATA_STATUS.NO_DEVICE -> {
                    binding.controlDevice.setText(R.string.go_on_device)
                }

                DevicesViewModel.DATA_STATUS.DATA -> {
                    binding.controlDevice.setText(R.string.stop_device)
                }
            }
        }

        viewModel.resultValue.observe(viewLifecycleOwner){data ->
            showData(data)
        }
    }

    private fun initView() {
        binding.apply {
            controlDevice.setOnClickListener {
                if (viewModel.connectStatus.value != BluetoothAdapter.STATE_CONNECTED) {
                    findNavController().navigate(R.id.DevicesFragment)
                }
            }
            fileList.layoutManager = LinearLayoutManager(context)
            fileList.adapter = HistoryAdapter()
        }
    }

    private fun showData(data: BaseData){
        for (i in data.getData()[0]){
            binding.lineChart.addEntry(i)
        }
    }

    class HistoryAdapter: Adapter<HistoryViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

    }

    class HistoryViewHolder(itemView: View) : ViewHolder(itemView){

    }

    data class HistoryFile(
        val id: Int,

    )

}