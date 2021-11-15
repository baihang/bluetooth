package com.example.healthy.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthy.chart.MyLineChart
import com.example.healthy.data.BaseData
import com.example.healthy.data.HeartOneData
import com.example.healthy.data.HeartSevenData
import com.example.healthy.data.HeartThreeData
import com.example.healthy.databinding.SevenFragmentBinding
import com.example.healthy.utils.*
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class SevenFragment() : Fragment() {

    companion object {
        private val colors = arrayOf(Color.RED, Color.BLUE, Color.CYAN)
        private const val TAG = "SevenFragment"
    }


    private val viewModel: DevicesViewModel by activityViewModels()
    private var binding: SevenFragmentBinding? = null
    private val viewList = CopyOnWriteArrayList<MyLineChart>()
    private val layoutManager = GridLayoutManager(context, 2)

    private val adapter = MyAdapter(viewList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SevenFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = adapter.getItemViewType(position)
                if (type == 1) {
                    return 2
                }
                return 1
            }
        })
        binding?.dataList?.layoutManager = layoutManager
        binding?.dataList?.adapter = adapter

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            if (data is HeartThreeData) {
                val seven = HeartSevenData()
                seven.bodyData = data.bodyData
                addValue(seven)
            } else {
                addValue(data)
            }
        }

        binding?.testButton?.setOnClickListener {
            val heart = HeartThreeData()
            for (i in heart.bodyData.indices) {
                heart.bodyData[i] = (Math.random() * 10).toInt()
            }
            viewModel.resultValue.value = heart
        }

        binding?.testButton?.setOnLongClickListener {
            selectFile()
            true
        }

    }

    private lateinit var client: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        client = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                openFile(it)
            })
    }

    private fun openFile(uri: Uri?) {
        if (uri?.path.isNullOrEmpty()) {
            return
        }
        val stream = FileInputStream(File(uri?.encodedPath)).buffered()
        while (stream.available() > 0) {
            val data = stream.readBytes()
            Log.e(TAG, "read = ${data.contentToString()}")
        }
    }

    private fun selectFile() {
        try {
            client.launch("text/plain")
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(requireView(), "打开资源管理器失败！", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun resetConfig() {
        val sp = SharedPreferenceUtil.getSharedPreference(context)
        val max = sp?.getInt(SettingViewModel.LIMIT_MAX, -1) ?: -1
        val min = sp?.getInt(SettingViewModel.LIMIT_MIN, -1) ?: -1
        val type = sp?.getInt(SettingViewModel.LIMIT_TYPE, 0) ?: 0
    }

    override fun onResume() {
        super.onResume()
        resetConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun addValue(data: BaseData) {
//        adapter.setData(data as HeartSevenData)
//        adapter.notifyDataSetChanged()
        if (viewList.size == 0) {
            return
        }
        val values = data.getData()
        for (i in values.indices) {
            when(i){
//                2 -> viewList[2].addEntry(values[6][0].toFloat() / 10000)
//                6 -> viewList[6].addEntry(values[2][0].toFloat() / 10000)
                else -> viewList[i].addEntry(values[i][0].toFloat() / 10000)
            }
        }
    }


    private class MyAdapter(viewList: CopyOnWriteArrayList<MyLineChart>) :
        RecyclerView.Adapter<MyViewHolder>() {
        var data: BaseData = HeartSevenData()
        var viewList: CopyOnWriteArrayList<MyLineChart>

        init {
            this.viewList = viewList
            viewList.clear()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val lineChart = MyLineChart(parent.context)
            val param = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500)
            lineChart.layoutParams = param
            lineChart.maxXAxisLength = 400
            lineChart.initDataSet(HeartSevenData.lables[viewList.size % 7], Color.RED)
            viewList.add(lineChart)
            return MyViewHolder(lineChart)
        }

        fun setData(data: HeartSevenData) {
            this.data = data
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            //holder.setData(data.getData()[position][0])
        }

        override fun getItemCount(): Int {
            return data.valueArray.size
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 6) {
                return 1
            }
            return 0
        }

    }

    private class MyViewHolder(itemView: MyLineChart) : RecyclerView.ViewHolder(itemView) {

        fun setData(value: Int) {
            (itemView as MyLineChart).addEntry(value)
        }

        fun setXMax(x: Int) {
            (itemView as MyLineChart).maxXAxisLength = x
        }

    }
}