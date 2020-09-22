package com.example.healthy.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.chart.MyChartData
import com.example.healthy.data.BaseData
import com.example.healthy.data.HeartOneData
import com.example.healthy.data.HeartThreeData
import com.example.healthy.databinding.MainFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.concurrent.CopyOnWriteArrayList

class MainFragment() : Fragment() {

    companion object {
        private val colors = arrayOf(Color.RED, Color.BLUE, Color.CYAN)
        private const val TAG = "MainFragment"
    }


    private val viewModel: DevicesViewModel by activityViewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLineChart()
        binding.mainSetting.setOnClickListener {
            val heart = HeartThreeData()
            for (i in heart.bodyData.indices) {
                heart.bodyData[i] = (Math.random() * 10).toShort()
            }
            viewModel.resultValue.value = heart
            viewModel.testTime(heart)

        }

        binding.mainBluetooth.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_DevicesFragment)
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            addValue(data)
        }

        viewModel.timeStampLive.observe(viewLifecycleOwner, Observer {
            val data = 60000 / it / 10L
            Log.e(TAG, "it = $it value = $data")
            binding.mainFlow.text = "流量： $data p/s"
        })

    }

    private fun initLineChart() {
        binding.lineChart1.initDataSet("心电 #1", colors[0])
        binding.lineChart2.initDataSet("心电 #2", colors[1])
        binding.lineChart3.initDataSet("心电 #3", colors[2])
    }

    private fun addValue(data: BaseData) {
        val dataArray = data.getData()
        if (dataArray.size == 3) {
            if (binding.lineChart2.visibility == View.GONE) {
                binding.lineChart2.visibility = View.VISIBLE
                binding.lineChart3.visibility = View.VISIBLE
            }
            for (i in dataArray[0].indices step 3) {
                binding.lineChart1.addEntry(dataArray[0][i])
                binding.lineChart2.addEntry(dataArray[1][i])
                binding.lineChart3.addEntry(dataArray[2][i])
            }
        } else {
            if (binding.lineChart2.visibility == View.VISIBLE) {
                binding.lineChart2.visibility = View.GONE
                binding.lineChart3.visibility = View.GONE
            }

            for (i in dataArray[0].indices) {
                binding.lineChart1.addEntry(dataArray[0][i])
            }
        }
    }
}