package com.example.healthy.ui.main

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
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
import com.example.healthy.utils.LocalFileUtil
import com.example.healthy.utils.ThreadUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

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

        binding.mainSaveBt.setOnClickListener {
            if (timeInterval == -1) {
//                开始保存
                timeInterval = 0
                binding.mainSaveBt.setText("结束保存")
                ThreadUtil.getInstance()?.addTimeListener(timeListener)
            } else {
//                保存结束
                binding.mainSaveBt.setText("开始保存")
                timeInterval = -1
                ThreadUtil.getInstance()?.removeTimeListener(timeListener)
                saveToFile()
                outPutStream?.close()
                outPutStream = null
                stringBuilder.clear()

                Snackbar.make(binding.mainSaveBt, filePath ?: "", Snackbar.LENGTH_LONG)
                    .setAction("复制", View.OnClickListener {
                        //复制到剪切板
                        val clipboard =
                            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val data =
                            ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, filePath)
                        clipboard.setPrimaryClip(data)
                    }).show()
            }
//            binding.mainSaveTimeTv.text = LocalFileUtil.getDateStr()
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            addValue(data)
        }

        viewModel.timeStampLive.observe(viewLifecycleOwner, Observer {
            val data = 10000 / it
            Log.e(TAG, "it = $it value = $data")
            binding.mainFlow.text = "流量： $data p/s"
        })

    }

    private fun initLineChart() {
        binding.lineChart1.initDataSet("心电 #1", colors[0])
        binding.lineChart2.initDataSet("心电 #2", colors[1])
        binding.lineChart3.initDataSet("心电 #3", colors[2])
    }

    /**
     * 10s保存一次
     */
    private var timeInterval = -1

    private val timeListener = object : ThreadUtil.TimeListener {
        override fun onClock() {
            timeInterval++
            if (timeInterval >= 10) {
                timeInterval = 0
                saveToFile()
            }
        }
    }

    private val stringBuilder = StringBuilder()
    private var filePath: String? = null
    private var outPutStream: FileOutputStream? = null
    private fun saveToFile() {
        Log.e(TAG, "save to file")
        var value = ""
        synchronized(stringBuilder) {
            value = stringBuilder.toString()
            stringBuilder.clear()
        }
        val result = "${LocalFileUtil.getDateStr()}\n$value\n"
        if (outPutStream == null) {
            val file =
                LocalFileUtil.createFile(context, "heart", "${LocalFileUtil.getDateStr()}.txt")
            filePath = file?.absolutePath
            Log.e(TAG, "open file = ${file?.absolutePath}")
            outPutStream = FileOutputStream(file)
        }
        outPutStream?.write(result.toByteArray())
        outPutStream?.flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (outPutStream != null) {
            outPutStream?.close()
        }
        ThreadUtil.getInstance()?.removeTimeListener(timeListener)
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

                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                    stringBuilder.append(dataArray[1][i]).append(" ")
                    stringBuilder.append(dataArray[2][i]).append(" ")
                }
            }
        } else {
            if (binding.lineChart2.visibility == View.VISIBLE) {
                binding.lineChart2.visibility = View.GONE
                binding.lineChart3.visibility = View.GONE
            }

            for (i in dataArray[0].indices) {
                binding.lineChart1.addEntry(dataArray[0][i])
                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                }
            }
        }
    }
}