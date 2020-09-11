package com.example.healthy.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.data.BaseData
import com.example.healthy.data.HeartOneData
import com.example.healthy.data.HeartThreeData
import com.example.healthy.databinding.MainFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

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
                heart.bodyData[i] = i.toShort()
            }
            viewModel.resultValue.value = heart
        }

        binding.mainBluetooth.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_DevicesFragment)
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            addValue(data)
        }
    }

    private fun initLineChart() {
        initLineChart(binding.lineChart1, addDataSet("心电 #1", colors[0], true))
        initLineChart(binding.lineChart2, addDataSet("心电 #2", colors[1], true))
        initLineChart(binding.lineChart3, addDataSet("心电 #3", colors[2], true))
    }

    private fun initLineChart(chart: LineChart, dataSet: LineDataSet) {
        chart.isEnabled = false
        chart.setDrawGridBackground(true)

        chart.data = LineData()
        chart.keepScreenOn = true

        chart.data.dataSets.add(dataSet)

        chart.invalidate()
    }

    private fun addValue(data: BaseData) {
        val dataArray = data.getData()
        if (dataArray.size == 3) {
            for (i in dataArray[0].indices step 3) {
                dataSetAddEntry(binding.lineChart1, dataArray[0][i])
                dataSetAddEntry(binding.lineChart2, dataArray[1][i])
                dataSetAddEntry(binding.lineChart3, dataArray[2][i])
            }
        } else {
            for (i in dataArray[0].indices) {
                dataSetAddEntry(binding.lineChart1, dataArray[0][i])
            }
        }
    }

    private fun addDataSet(label: String, color: Int, fill: Boolean): LineDataSet {
        val dataSet = LineDataSet(null, label)
        dataSet.cubicIntensity = 0.2F
        dataSet.lineWidth = 1.5f
        dataSet.color = color
        dataSet.setDrawCircles(false)
        dataSet.fillColor = Color.GRAY
        dataSet.setDrawFilled(fill)
        return dataSet
    }

    private fun dataSetAddEntry(chart: LineChart, value: Int) {
        val dataSet = chart.data.dataSets[0]
        dataSetAddEntry(dataSet, value)
        chart.data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun dataSetAddEntry(dataSet: ILineDataSet, value: Int) {
        if (dataSet.entryCount >= 100) {
            dataSet.removeFirst()
        }
        if (dataSet.entryCount == 0) {
            dataSet.addEntry(Entry(0f, value.toFloat()))
            return
        }
        val last = dataSet.getEntryForIndex(dataSet.entryCount - 1)
        val entry = Entry(last.x + 1, value.toFloat())
        if (entry.x < 0) {
            //避免溢出后为负数
            entry.x = 0f
        }
        dataSet.addEntry(entry)

    }

}