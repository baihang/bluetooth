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
import com.example.healthy.databinding.MainFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MainFragment() : Fragment() {

    companion object {

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
            val value = (Math.random() * 100 + 20).toInt()
            addValue(value)
        }

        binding.mainBluetooth.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_DevicesFragment)
        }

        viewModel.resultValue.observe(viewLifecycleOwner) {data ->

        }
    }

    private fun initLineChart() {
        val chart = binding.lineChart
        chart.isEnabled = false
        chart.setDrawGridBackground(true)

        addValue(chart)
        chart.keepScreenOn = true

        chart.invalidate()

    }

    private fun addValue(chart: LineChart) {

        val values = java.util.ArrayList<Entry>()

        for (i in 0 until 100) {
            val t = (Math.random() * (100 + 1)).toFloat() + 20
            values.add(Entry(i.toFloat(), t))
        }

        val dataSet = LineDataSet(values, "heart")
        dataSet.cubicIntensity = 0.2F
        dataSet.lineWidth = 1.5f
        dataSet.color = Color.BLACK
        dataSet.setDrawCircles(false)
        dataSet.fillColor = Color.GRAY
        dataSet.setDrawFilled(true)

        val lineData = LineData(dataSet)
        chart.data = lineData

    }

    private fun addValue(value: Int) {
        val dataSet = binding.lineChart.data.dataSets[0]
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
        binding.lineChart.data.notifyDataChanged()
        binding.lineChart.notifyDataSetChanged()
        binding.lineChart.invalidate()
    }


}