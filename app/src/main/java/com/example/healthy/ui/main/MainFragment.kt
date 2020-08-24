package com.example.healthy.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

            val dataSet = binding.lineChart.data.dataSets[0]
            val value: Float = (Math.random() + 20).toFloat()
            dataSet.removeEntry(0)
            val last = dataSet.getEntryForIndex(dataSet.entryCount - 1)
            val entry = Entry(last.x + 1, value)
            dataSet.addEntry(entry)
            binding.lineChart.data.notifyDataChanged()
            binding.lineChart.notifyDataSetChanged()
            binding.lineChart.invalidate()
        }

        binding.mainBluetooth.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_DevicesFragment)
        }
    }

    private fun initLineChart() {
        val chart = binding.lineChart
        chart.isEnabled = false
        chart.setDrawGridBackground(true)

        val dataSet: LineDataSet = LineDataSet(ArrayList(), "heart")
        chart.data = LineData(dataSet)

        addValue(chart)

        chart.invalidate()

    }

    private fun addValue(chart: LineChart) {

        val values = java.util.ArrayList<Entry>()

        for (i in 0 until 100) {
            val t = (Math.random() * (100 + 1)).toFloat() + 20
            values.add(Entry(i.toFloat(), t))
        }

        val dataSet = LineDataSet(values, "dataSet")
        dataSet.cubicIntensity = 0.2F
        dataSet.lineWidth = 1.8f
        dataSet.color = Color.BLACK

        val lineData = LineData(dataSet)
        chart.data = lineData

    }


}